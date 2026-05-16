# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

`ms-security-bff` is the **Security BFF** of a multi-tenant pharmacy management system. It owns authentication, JWT issuance/validation, session management, the 3D permission model (role × resource × action × scope), token storage, audit log, and rate limiting. OTP infrastructure (table `codigos_otp`, entity) está creada pero el flujo aún **no está implementado**. It backs the PostgreSQL database `poc-security` (schema `sec`).

A sibling **Business BFF** (`ms-business-bff`) owns domain logic (inventory, sales/POS, customers, prescriptions, suppliers) against `db_negocio`. Both BFFs are independent Gradle projects that share the same hexagonal layout and the same multi-tenant strategy (row-level `empresa_id` + `sucursal_id`).

## Stack

- Java 21 (toolchain). **Virtual Threads no están habilitados todavía** — pendiente del plan (P4.2).
- Spring Boot 4.0.6 + Spring Security 6 + Spring Data JPA + Validation + WebMVC.
- Jackson 3 (`tools.jackson.*`) — Spring Boot 4 lo trae vía `spring-boot-starter-json`. JJWT mantiene su Jackson 2 transitivo aislado.
- JJWT 0.12.6 (firma HS256), Bucket4j 8.10 + Caffeine (rate-limit in-memory), AspectJ (TenantFilterAspect).
- PostgreSQL driver runtime.
- Lombok (compileOnly + annotationProcessor).
- Gradle 9.4.1 wrapper. Build script todavía es `build.gradle` Groovy; el target es `build.gradle.kts` + `libs.versions.toml`.
- JUnit 5 (`useJUnitPlatform()`). No hay tests propios todavía — solo el smoke test autogenerado.
- **Spotless 7.0.4** (`com.diffplug.spotless`) con `googleJavaFormat 1.27.0` + `removeUnusedImports` + `trimTrailingWhitespace`. **Checkstyle 10.20.2** con ruleset propio en `config/checkstyle/checkstyle.xml`. Ambos enlazados a `check`.
- **JSpecify 1.0.0** (`org.jspecify.annotations.{NonNull,Nullable,NullMarked}`) viene transitivamente vía `spring-core`. Es el estándar de nullability del proyecto — NO usar `org.springframework.lang.{NonNull,Nullable}` (deprecated desde Spring 7).

## Common commands

Use the wrapper (`./gradlew` on Unix shells, `gradlew.bat` on cmd). All commands run from the repo root.

```bash
./gradlew build              # full build (compile + test + check + assemble)
./gradlew bootRun            # run the Spring Boot app
./gradlew test               # all tests
./gradlew test --tests "pe.com.mcc.security.MsSecurityBffApplicationTests"   # single test class
./gradlew test --tests "*.SomeTest.someMethod"                                # single test method
./gradlew clean
./gradlew dependencies       # dependency tree (use --configuration runtimeClasspath to narrow)

# Code quality gates (parte de `check`):
./gradlew spotlessCheck      # falla si el formato no está aplicado
./gradlew spotlessApply      # auto-formatea (google-java-format + import-order + trim)
./gradlew checkstyleMain     # valida ruleset src/main
./gradlew checkstyleTest     # valida ruleset src/test
./gradlew pmdMain            # analiza src/main (bugs, código muerto, seguridad)
./gradlew pmdTest            # analiza src/test
./gradlew check              # equivalente a tests + spotlessCheck + checkstyleMain + checkstyleTest + pmdMain + pmdTest
```

The app expects PostgreSQL at `localhost:5432`, database `poc-security`, schema `sec`. Credentials read from `DB_USERNAME` / `DB_PASSWORD` env vars (defaults in `application.yaml` are dev-only). Schema `sec` y extensiones `pgcrypto` / `uuid-ossp` se crean automáticamente desde `schema.sql`; las 10 tablas las crea Hibernate (`ddl-auto: create-drop`); `data.sql` siembra los datos demo.

## Architecture — hexagonal, vertical-sliced

The project uses **strict hexagonal architecture with vertical slicing by bounded context**. This applies to **both BFFs**, even though the original requirement document only mentioned hexagonal for the Business BFF — coherence between the two services is a hard constraint.

Three top-level layers under `pe.com.mcc.security`:

```
domain/           Pure Java. No Spring, no JPA, no framework annotations.
                  Models, value objects, domain events, domain exceptions.
application/      Use cases (port/in) + outbound ports (port/out) + use case
                  implementations. Orchestrates the domain. No frameworks.
infrastructure/   All adapters and Spring/JPA/Resilience4j wiring.
                  in/  → web controllers, security filters, schedulers, event listeners.
                  out/ → JPA persistence, notification channels, JWT signer/decoder,
                         crypto, event publisher, rate-limit store.
                  config/, tenant/, audit/ → cross-cutting infra.
```

Inside each layer, code is grouped by bounded context: `auth`, `token`, `otp`, `user`, `permission`, `notification`, `audit`, `tenant`, `shared`. A change to e.g. OTP touches `domain/otp`, `application/otp`, and `infrastructure/adapter/{in,out}/...` — never spans into another bounded context.

**Rules of thumb when adding code:**
- Domain entities (`@Entity`) live in `infrastructure/adapter/out/persistence/<context>/` as `*JpaEntity` and are mapped to/from pure domain models via `*PersistenceAdapter` classes that implement the outbound port defined in `application/<context>/port/out/`.
- Controllers live in `infrastructure/adapter/in/web/<context>/`, with DTOs and DTO↔domain mappers under that same package. Inject use case interfaces from `application/<context>/port/in/`, never `*Service` impls directly.
- Domain events are published via an `EventPublisher` outbound port (`application/shared/port/out`) implemented by `infrastructure/adapter/out/event/EventPublisherAdapter` (which delegates to Spring's `ApplicationEventPublisher`). `@Async` listeners go in `infrastructure/adapter/in/event/`.
- Strategy pattern for notifications: the `NotificationChannel` port in `application/notification/port/out/` has Email/SMS/WhatsApp implementations under `infrastructure/adapter/out/notification/{email,sms,whatsapp}/`, selected by a `NotificationChannelFactory`. Each channel is wrapped with Resilience4j `@CircuitBreaker` + `@Retry` + `@Bulkhead` and falls back to email.

Do **not** propose or introduce a flat "modular by feature" layout (e.g. `auth/{api,service,domain,repository}`). The user has explicitly rejected that — see `memory/feedback_hexagonal_both_bffs.md`.

## Multi-tenancy

Row-level tenancy. Every business table carries `empresa_id UUID NOT NULL` and `sucursal_id UUID NOT NULL`. Mechanism:

- `TenantContext` (in `infrastructure/tenant/`) is a `ThreadLocal<TenantInfo>` populated from the JWT by a request filter.
- A Hibernate `@Filter(name = "tenantFilter", condition = "empresa_id = :tenantId")` is declared on a `BaseTenantJpaEntity` mapped superclass under `infrastructure/adapter/out/persistence/shared/`.
- A `TenantFilterAspect` reads `TenantContext` and activates the filter on every transaction. Use cases, repositories, and services must **never** reference `empresa_id` explicitly; the filter applies it transparently.
- Branch switch (`POST /auth/switch-branch/{sucursalId}`) revokes the **whole current `sesion_id`** (access + refresh) and issues a fresh pair under a new `sesion_id` carrying the new active `sucursal_id`, then publishes `BranchSwitchedEvent`. Cross-branch reads require the `STOCK_READ_ALL_BRANCHES` permission.

## Permissions — 3D model (role × resource × action × scope)

Stored in `sec.permisos_rol_recurso` with columns `rol_id`, `recurso` (e.g. `PRODUCT`, `STOCK`, `SALE`, `CLIENT`, `USER`, `REPORT`, `SUPPLIER`, `PURCHASE_ORDER`, `PRESCRIPTION`, `AUDIT`, `*`), `acciones VARCHAR[]` (subset of `READ/CREATE/UPDATE/DELETE`), and `alcance` (`OWN_BRANCH` | `ALL_BRANCHES`). Roles: `SUPER_ADMIN`, `ADMIN_EMPRESA`, `ADMIN_SUCURSAL`, `FARMACEUTICO`, `CAJERO`, `AUDITOR`.

`SUPER_ADMIN` is encoded with `recurso = '*'` (a single wildcard row) instead of one row per resource — the `PermissionEvaluator` must treat `*` as "any resource".

A custom `PermissionEvaluator` (in `infrastructure/adapter/out/security/` or wired via `application/permission/`) evaluates "does this role have action X on resource Y in the active alcance?". The full permission map for the user is serialized into the JWT payload so the Angular front-end can render UI without extra round-trips. Endpoints are guarded with `@PreAuthorize("hasAnyRole(...)")` and/or `hasPermission(...)` expressions.

## JWT, token store, OTP

Persistence tables live in schema `sec` with Spanish names: `tokens`, `codigos_otp`, `bitacora_auditoria`, `usuarios`, `roles`, `permisos_rol_recurso`, etc. Enum-like values inside `CHECK` constraints stay in English (`READ/CREATE/UPDATE/DELETE`, `OWN_BRANCH/ALL_BRANCHES`, `ACCESS/REFRESH`, ...) because they are tokens of the security model that flow through the JWT, `@PreAuthorize`, and the `PermissionEvaluator`.

- Every issued JWT has a `jti` recorded in `sec.tokens` along with `usuario_id`, `empresa_id`, `sucursal_id`, `huella_dispositivo`, `direccion_ip`, `agente_usuario`, `emitido_en`, `expira_en`, `revocado`, `revocado_en`, `motivo_revocacion`. `JwtAuthenticationFilter` validates `jti` against `sec.tokens` **before** building the `SecurityContext` — a revoked `jti` rejects the request even if the JWT signature is valid.
- `UserPrincipal` carries the enriched principal: user, role, permission map, available branches, active branch, `empresa_id`.
- **Session model.** Every login generates a `sesion_id` (UUID). The access token and the refresh token issued in that login share the same `sesion_id`. This is the primary handle for invalidating "the whole session" instead of just one token.
- **Logout flow** (single device). Marks every active token of the current `sesion_id` as revoked in one statement:
  ```sql
  UPDATE sec.tokens
     SET revocado = TRUE, revocado_en = NOW(), motivo_revocacion = 'LOGOUT'
   WHERE sesion_id = :sesionId AND revocado = FALSE;
  ```
  This kills both the access JTI (so the next request fails the `JwtAuthenticationFilter` check) and the paired refresh JTI (so the client cannot mint a new access token). `LogoutService` reads `sesion_id` from the access token's claims and runs the update inside a `@Transactional` block, then publishes `TokenRevokedEvent` for audit.
- **Logout-all** (every device). Same query without the `sesion_id` filter, scoped by `usuario_id` and using `motivo_revocacion = 'LOGOUT_ALL'`. Useful from an admin endpoint or after a password change.
- **Branch switch** revokes the current `sesion_id` (`motivo_revocacion = 'BRANCH_SWITCH'`) and issues a fresh pair of tokens with a new `sesion_id` that carries the new active `sucursal_id`.
- Refresh-token rotation: when a refresh JTI is consumed, mark it `REFRESH_USED` and emit a new access+refresh pair under the **same** `sesion_id` (so a future logout still wipes everything in one go).
- **Lockout de cuenta**. `Usuario.MAX_INTENTOS_FALLIDOS = 5`. Cada password incorrecto incrementa `intentos_fallidos`; al llegar a 5, la cuenta queda `estado = BLOQUEADO` con `bloqueado_hasta = now + 15min`. `Usuario.estaBloqueado(now)` respeta el tiempo: si `bloqueado_hasta` ya pasó, retorna false aunque `estado=BLOQUEADO`; el primer login exitoso post-bloqueo deja `estado=ACTIVO` y resetea el contador. Crítico: el incremento se persiste vía `RegistrarIntentoFallidoService` con `@Transactional(REQUIRES_NEW)` — si lo hicieras dentro del `AuthenticateService` (que también es `@Transactional`), el `throw InvalidCredentialsException` haría rollback y el contador nunca avanzaría.
- **OTP — pendiente**. La tabla `sec.codigos_otp` y `CodigoOtpJpaEntity` ya existen, pero **no hay** `OtpService`, `OtpVerifier`, `OtpCleanupScheduler`, ni los endpoints `/auth/otp/request` y `/auth/otp/verify`. Cuando se implementen: `codigo_hash` con BCrypt, generación `SecureRandom` 6 dígitos, verificación con `@Lock(PESSIMISTIC_WRITE)`, `intentos_maximos=3`, `expira_en=5min`, `OtpCleanupScheduler` con `@Scheduled`. La política rate-limit `otp-verify` ya está declarada en `application.yaml` esperando esos endpoints.

## Cross-cutting

- **Auditing** — `SecurityAuditorAware implements AuditorAware<String>` (in `infrastructure/audit/`) reads the user from `SecurityContext`. JPA entities extend `BaseAuditableJpaEntity` annotated with `@EntityListeners(AuditingEntityListener.class)` and the `@CreatedBy/@LastModifiedBy/@CreatedDate/@LastModifiedDate` quartet, persisted as `creado_por / fecha_creacion / modificado_por / fecha_modificacion`. `@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")`. The append-only `sec.bitacora_auditoria` table stores `valor_anterior` / `valor_nuevo` as `jsonb` plus `accion` (CREATE/UPDATE/DELETE/LOGIN_SUCCESS/LOGIN_FAILED/OTP_*/TOKEN_*/BRANCH_SWITCHED/...).
- **Rate limiting** — Bucket4j 8.x + Caffeine in-memory store. Detalles en la sección **Rate limiting** abajo.
- **Resilience** — Resilience4j on outbound calls (notification channels), with email as the fallback channel.
- **Logging** — JSON logs (Logback `JsonEncoder`) con MDC `traceId/spanId/userId/username/empresaId/sucursalId`. `MdcFilter` pone `traceId/spanId` al entrar, `TenantResolverFilter` añade `userId/empresaId/sucursalId` después del `JwtAuthenticationFilter`. Perfiles `dev/local/default` cambian a formato humano.
- **Errors** — RFC 9457 `ProblemDetail` con `traceId` desde MDC. Tres puntos cubren toda la cadena: `GlobalExceptionHandler` (`@RestControllerAdvice`) para excepciones que llegan al controller, `JwtAuthenticationEntryPoint` para 401 y `ForbiddenAccessDeniedHandler` para 403 desde el filter chain. `ProblemDetailWriter` sirve de helper común.
- **`LogoutContextCleanupInterceptor`** — `HandlerInterceptor` (no es un servlet filter) registrado solo para los paths de logout (`/auth/logout`, `/auth/logout-all`). Su `afterCompletion` llama a `SecurityContextHolder.clearContext()` siempre — tanto si el handler devolvió 204 como si lanzó excepción — garantizando que no quede ningún principal residual en el ThreadLocal si en el futuro se introduce post-procesamiento síncrono o se cambia a política stateful.
- **Endpoints públicos** (`permitAll()` en `SecurityConfig`): `POST /auth/login`, `POST /auth/refresh`, `/actuator/health`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`. Todo lo demás requiere autenticación.
- **Async** — `ThreadPoolTaskExecutor` dedicado `securityEventsExecutor` (`AsyncConfig`, core 2 / max 8 / queue 500 / `CallerRunsPolicy`) para listeners. Virtual Threads aún **no** están habilitados.

## Code quality gates

Tres gates protegen el repo. Todas se enlazan a `./gradlew check` (que se ejecuta dentro de `build`):

| Gate | Qué hace | Cómo arreglar |
|---|---|---|
| **Spotless** | Verifica que todos los `*.java`, `*.gradle`, `*.md`, `*.yaml` están formateados con `google-java-format 1.27.0`, sin imports no usados, sin trailing whitespace. | `./gradlew spotlessApply` aplica auto-fix in-place. |
| **Checkstyle** | Valida `config/checkstyle/checkstyle.xml` (naming, no-star-imports, no-tabs, line ≤ 140, NeedBraces + LITERAL_IF/CATCH, EmptyBlock + CATCH, MissingOverride, anti-`System.out`). `maxWarnings = 0`. | Edita el archivo según el reporte HTML que genera. |
| **PMD** | Analiza `config/pmd/pmd.xml` (bugs reales: EqualsNull, MisplacedNullCheck, CloseResource; código muerto: UnusedLocalVariable/Field/Method; seguridad: `security.xml` completo; concurrencia: DoubleCheckedLocking, NonThreadSafeSingleton; complejidad: CyclomaticComplexity ≤ 15). | Corrige la violación o usa `@SuppressWarnings("PMD.NombreDeLaRegla")` para falso positivo. |
| **JSpecify** | Anotaciones de nullability inferidas estáticamente. Spring 7 marca sus interfaces con `@NullMarked`, los overrides deben preservar/declarar `@Nullable` donde aplique. | Importa `org.jspecify.annotations.{Nullable,NullMarked}` y anota explícitamente. |

### Convenciones de formato impuestas

- **Indentación**: 2 espacios (estilo Google).
- **Imports ordenados**: java/javax/jakarta primero, luego lombok, luego org/com, luego pe.* del proyecto.
- **No star imports** (`import x.*` está prohibido por Checkstyle, incluido `jakarta.persistence.*`).
- **Líneas ≤ 140 caracteres** (Checkstyle).
- **`@Override` siempre que se sobrescriba un método.**
- **Sin tabs.**

### Convenciones de nullability con JSpecify

JSpecify es el sustituto oficial de `org.springframework.lang.{NonNull,Nullable}` (deprecated desde Spring 7).

| Patrón | Cuándo |
|---|---|
| `@NullMarked` a nivel de clase o `package-info` | Cuando todos los parámetros/returns son `@NonNull` por defecto. La forma idiomática para clases nuevas. |
| `@Nullable T param` | Parámetro que puede recibir null (sobrescribiendo el default `@NonNull` de `@NullMarked`). |
| `private static @Nullable T method(...)` | Método privado helper que retorna null en algún caso. |
| Sin anotación | Hereda el default del scope (`@NonNull` si la clase está `@NullMarked`). |

Casos prácticos en el proyecto:
- **`ThreeDPermissionEvaluator`** está `@NullMarked` y declara `@Nullable Authentication authentication` y `@Nullable Serializable targetId` en los overrides de `PermissionEvaluator` (Spring SpEL pasa null literalmente en `@PreAuthorize("hasPermission(null, 'USER', 'CREATE')")`).
- **Filtros en `infrastructure/adapter/in/security/`** declaran `@NonNull HttpServletRequest`, etc. en los `doFilterInternal(...)` para satisfacer `OncePerRequestFilter`.

## Rate limiting

Implementado con **Bucket4j 8.10 + Caffeine** (in-memory). Cada request a un endpoint con política configurada consume 1 token de un bucket identificado por `(policyName, key)`. Si el bucket queda en 0, el filter responde **429** con `Retry-After`, sin llegar al controller.

### Componentes

| Capa | Clase | Rol |
|---|---|---|
| Domain | `RateLimitPolicy` | Record `name + capacity + refillPeriod + keyedBy`. |
| Domain | `RateLimitDecision` | Record `allowed + retryAfterSeconds + remainingTokens`. |
| Domain | `KeyedBy` | Enum `IP \| USER_ID`. |
| Domain | `RateLimitExceededException` | Disponible para usecases (no se lanza desde el filter). |
| Application — port out | `RateLimitStore` | Contrato `tryConsume(policy, key) → Decision`. |
| Application — port in  | `CheckRateLimitUseCase` | Fachada delgada (DIP). |
| Application — usecase  | `CheckRateLimitService` | Delega al store. Hook futuro de métricas. |
| Infra — config         | `RateLimitProperties` | `@ConfigurationProperties("security.ratelimit")`. |
| Infra — adapter out    | `Bucket4jRateLimitAdapter` | `ConcurrentHashMap<policyName, Caffeine<key, Bucket>>`. Buckets idle se liberan a `2 × refillPeriod`. Comentarios `SEQ_01..04`. |
| Infra — adapter in     | `RateLimitFilter` | `OncePerRequestFilter` con `AntPathMatcher`. Ejecuta **después** del `JwtAuthenticationFilter` para que policies `USER_ID` lean el principal. Comentarios `SEQ_10..14`. |

### Políticas configuradas (`application.yaml`)

| Política | Path pattern | Capacidad | Período | Keyed by | Justificación |
|---|---|---|---|---|---|
| `login` | `/auth/login` | 5 | 1 min | IP | Defensa anti brute-force pre-autenticación. |
| `refresh` | `/auth/refresh` | 10 | 1 min | IP | Tolera refresh batch del front pero corta abuso. |
| `otp-verify` | `/auth/otp/verify` | 3 | 5 min | USER_ID (fallback IP) | OTP es 6 dígitos: 3 intentos en 5 min son suficientes para usuarios legítimos. |

`security.ratelimit.enabled: false` desactiva todo (debug). `whitelist-ips` lista IPs que omiten el rate-limit (health checks, CI, oficina).

### Headers de respuesta

| Header | Cuándo | Valor |
|---|---|---|
| `X-RateLimit-Limit` | siempre | `capacity` de la política aplicada |
| `X-RateLimit-Remaining` | siempre | tokens restantes tras consumir 1 |
| `Retry-After` | solo en 429 | segundos hasta el próximo refill (techo, mínimo 1) |
| `Content-Type` | en 429 | `application/problem+json` |

### Tabla de decisión del filter

| Condición | Acción |
|---|---|
| `enabled = false` o `policies` vacío | `chain.doFilter` (no-op). |
| IP del request en `whitelist-ips` | `chain.doFilter` (saltea). |
| Ningún `path-pattern` matchea el URI | `chain.doFilter` (endpoint no rate-limited). |
| Match + `keyedBy = IP` | `key = "ip:" + remoteAddr` (resp. X-Forwarded-For). |
| Match + `keyedBy = USER_ID` con principal | `key = "user:" + usuarioId`. |
| Match + `keyedBy = USER_ID` sin principal | `key = "ip:" + remoteAddr` (fallback conservador). |
| `tryConsume` allowed | Headers `X-RateLimit-*` + `chain.doFilter`. |
| `tryConsume` denied | 429 + `Retry-After` + ProblemDetail `urn:problem-type:rate-limit-exceeded`. |

### Forma del response 429

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 47
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
Content-Type: application/problem+json

{
  "type": "urn:problem-type:rate-limit-exceeded",
  "title": "Demasiadas peticiones",
  "status": 429,
  "detail": "Has superado el límite de peticiones para esta operación. Reintenta en 47 segundos.",
  "traceId": "a3f9b21c4d8e..."
}
```

### Para producción multi-instancia

`Bucket4jRateLimitAdapter` es in-memory: cada instancia mantiene su propio cache. En cluster, los límites por IP/usuario se aplican **por instancia** (no globalmente). Para corregir, sustituir el adapter por uno con `bucket4j-redis`. El `CheckRateLimitUseCase`, el filter y las properties no cambian — pure DIP.

## Database & migrations

SQL lives in `src/main/resources/db/migration/` with Flyway naming (`V1__init_security_schema.sql`, `V2__seed_security_data.sql`). Flyway is **not** wired into the build yet — when adding it, also flip `spring.jpa.hibernate.ddl-auto` from the current `create-drop` to `validate` so Hibernate doesn't fight the migrations.

Naming convention (this repo's hard rule):
- **Tables and columns: Spanish.** `usuarios`, `sucursales`, `permisos_rol_recurso`, `bitacora_auditoria`, `nombre_usuario`, `contrasena_hash`, `fecha_creacion`, `valor_anterior`, ... JPA entities and field names should mirror the SQL (or use `@Column(name = "...")` mappings).
- **Enum-like values inside `CHECK` constraints: English.** `READ/CREATE/UPDATE/DELETE`, `OWN_BRANCH/ALL_BRANCHES`, recursos (`PRODUCT/STOCK/...`), `ACCESS/REFRESH`, `LOGOUT/LOGOUT_ALL/BRANCH_SWITCH/REFRESH_USED/ADMIN_REVOKE/...`, OTP `EMAIL/SMS/WHATSAPP`, `LOGIN_2FA/RESET_PASSWORD/...`. Don't translate these — they appear in `@PreAuthorize`, JWT claims, and the `PermissionEvaluator`.
- **State enums: Spanish.** `ACTIVO/INACTIVO/SUSPENDIDO/BLOQUEADO/PENDIENTE_VERIFICACION/CERRADA`.

Demo data (loaded by `V2`):
- Universal password: `Demo2026!` (BCrypt via `pgcrypto`'s `crypt(..., gen_salt('bf', 10))`).
- Logins: `superadmin`, `admin.bsj`, `jefe.bsj.centro`, `farma.bsj`, `cajero.bsj`, `auditor.bsj` (Botica San Juan); `admin.fv`, `jefe.fv.plaza`, `farma.fv`, `cajero.fv`, `auditor.fv` (Farmacia Vida).
- 2 demo empresas with 2 sucursales each. `SUPER_ADMIN` has no `usuarios_sucursales` rows — it switches to any branch dynamically.

## Current state — qué está hecho

Lo construido hasta hoy (BFF Seguridad):

| Pieza | Estado |
|---|---|
| Esquema PostgreSQL `sec` con 10 tablas, índices y CHECKs | ✅ V1 + V2 (ref) y `schema.sql`/`data.sql` (runtime) |
| Entidades JPA + `BaseAuditableJpaEntity` + `BaseTenantJpaEntity` | ✅ |
| `JwtAuthenticationFilter` validando jti vs `sec.tokens` | ✅ |
| Endpoints `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`, `/auth/switch-branch/{sucursalId}` | ✅ |
| `sesion_id` modelo (login/refresh comparten · branch-switch crea nueva · logout revoca toda) | ✅ |
| 3D `PermissionEvaluator` + `@PreAuthorize("hasPermission(...)")` cableado vía `MethodSecurityConfig` | ✅ |
| Endpoints `GET /usuarios`, `POST /usuarios` con `@PreAuthorize` (demo del 3D) | ✅ |
| Multi-tenant: `TenantContext` + `TenantResolverFilter` + `TenantFilterAspect` (`@EnableTransactionManagement(order=0)` para que el aspect quede inside del `@Transactional`) | ✅ |
| `@Filter` Hibernate aplicado en `UsuarioJpaEntity` y `SucursalJpaEntity` | ✅ |
| Eventos `LoginSuccessEvent` · `LoginFailedEvent` · `TokenRevokedEvent` · `BranchSwitchedEvent` | ✅ |
| Listeners `@Async` que persisten en `sec.bitacora_auditoria` (`LoginAuditListener`, `TokenRevokedAuditListener`, `BranchSwitchedAuditListener`) | ✅ |
| MDC + Logback `JsonEncoder` + perfiles dev/prod | ✅ |
| `ProblemDetail` RFC 9457 (handler + entry-point + access-denied-handler con `traceId`) | ✅ |
| Lockout de cuenta a 5 fallos · `RegistrarIntentoFallidoService` con `REQUIRES_NEW` | ✅ |
| Rate-limit Bucket4j + Caffeine (`login` 5/min IP · `refresh` 10/min IP · `otp-verify` 3/5min USER_ID) | ✅ |
| Colección Postman `postman/ms-security-bff.postman_collection.json` con auto-captura de tokens | ✅ |
| **Notification Strategy** (Email/SMS/WhatsApp + Resilience4j fallback): directorios scaffoldeados en `infrastructure/adapter/out/notification/{email,sms,whatsapp,factory,resilience}/` pero **sin implementación** | ❌ pendiente |
| **OTP completo, Resilience4j, OpenAPI, Tests, Flyway, Virtual Threads, Gradle KTS** | ❌ pendientes |

**Pendientes priorizados** (orden recomendado): P3.3 Flyway → P2.1 Notification Strategy → P1.2 OTP completo → P3.4 OTP en login → P3.1 OpenAPI → P3.2 Tests → P4.1 Gradle KTS → P4.2 Virtual Threads.

### Plantilla para añadir un feature nuevo

1. Domain model + events/exceptions en `domain/<contexto>/`.
2. Inbound port (`UseCase` interface) en `application/<contexto>/port/in/`.
3. Outbound ports (repository / gateway) en `application/<contexto>/port/out/`.
4. Use case impl en `application/<contexto>/usecase/`.
5. `JpaEntity` + `JpaRepository` + `PersistenceAdapter` en `infrastructure/adapter/out/persistence/<contexto>/`.
6. Controller (thin: solo orquesta) + DTO + mapper en `infrastructure/adapter/in/web/<contexto>/`, con `@PreAuthorize("hasPermission(...)")`.
7. Adapters externos (notification, JWT, crypto) en `infrastructure/adapter/out/...`.
8. Listeners `@Async` para reaccionar a eventos en `infrastructure/adapter/in/event/`.

**No** poner lógica en el controller. Toda transformación, validación condicional o lectura de headers va en componentes separados (`HttpRequestContextResolver`, mappers, casos de uso). Memoria explícita: `feedback_controllers_thin.md`.

## Notes

- **Spring Security 6 (Spring Boot 4)**: `addFilterBefore`/`addFilterAfter` exigen que la clase de referencia esté en `FilterOrderRegistration` (filtros estándar). No se puede usar un custom filter como ancla — todos van anclados a `UsernamePasswordAuthenticationFilter`. El orden real de ejecución sigue el orden de inserción en el `HttpSecurity` builder:
  1. `MdcFilter` — traceId/spanId disponibles en todos los logs siguientes.
  2. `JwtAuthenticationFilter` — valida la firma + jti y popula el `SecurityContext` con `UserPrincipal`.
  3. `TenantResolverFilter` — pobla `TenantContext` + MDC con userId/empresaId/sucursalId (necesita el principal del paso anterior).
  4. `RateLimitFilter` — DESPUÉS del JWT para que las políticas `USER_ID` lean el principal ya disponible.
- **Jackson 2 vs 3**: el `ObjectMapper` autoconfigurado por Spring Boot 4 está en `tools.jackson.databind.*` (Jackson 3). NO usar `com.fasterxml.jackson.databind.*` para inyectar el bean — `com.fasterxml.*` solo existe transitivamente vía `jjwt-jackson` y aislado al uso interno de JJWT.
- **`@Transactional` y rollback**: cualquier persistencia que deba sobrevivir al `throw` de un `@Transactional` parent debe ir en un servicio anotado con `@Transactional(propagation = REQUIRES_NEW)`. Patrones aplicados: `RegistrarIntentoFallidoService` (lockout), `RecordAuditService` (bitácora). Si añades algo similar, sigue ese patrón.
- **Comentarios `SEQ_NN`** en `Bucket4jRateLimitAdapter` (`SEQ_01..04`) y `RateLimitFilter` (`SEQ_10..14`) — convención del requerimiento original para puntos auditables.
- **Bucket4j 8.x — usar el builder fluent**: `Bandwidth.builder().capacity(N).refillIntervally(N, period).build()`. NO usar `Bandwidth.classic(...)` ni `Refill.intervally(...)` — están deprecated.
- **Restricted identifiers (Java 14+)**: no usar `record`, `yield`, `sealed`, `permits`, `non-sealed` como nombres de método/variable. Aunque compilan, SonarLint los marca (regla S6217). Convención del proyecto: usar verbos en español tipo `registrar(...)` (ver `RecordAuditUseCase`, `RegistrarIntentoFallidoUseCase`).
- **Constant Interface Anti-Pattern (S1214)**: NUNCA agrupar constantes en una `interface` (ni `private`). Patrones correctos en este repo: clase final con constructor privado (`MdcConstants`, `Recurso`), enum (`Accion`, `Alcance`, `TipoToken`), o importar directamente la constante de su origen (`BeanDefinition.ROLE_INFRASTRUCTURE` se usa importando `org.springframework.beans.factory.config.BeanDefinition`).
- **Nested ternary (S3358)**: extraer a método helper con early-return. Ejemplo: `TokenRevokedAuditListener.resolverEntidadId(event)`.
- **Casts unchecked (`@SuppressWarnings("unchecked")`)**: refactorizar a **pattern matching `instanceof`** de Java 21 cuando sea posible. Ejemplo: `JwtTokenDecoderAdapter.deserializePermisos(...)` usa `instanceof Map<?, ?> outer` en vez de cast a `Map<String, Map<String, Object>>`.
- `build.gradle` (Groovy) sigue siendo el build script. El target es `build.gradle.kts` + `gradle/libs.versions.toml`.
- El repo **no** es git (`git init` no se ha ejecutado). No asumir comandos `git` disponibles.
- Windows: usa bash via la Bash tool (forward slashes, `/dev/null`); PowerShell disponible cuando se requiera operación específica de Windows.
