package pe.com.mcc.security.domain.token.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.permission.model.PermissionMap;

/**
 * Claims tipados que viajan en el JWT. Reflejan exactamente lo que el JwtAuthenticationFilter
 * necesita para construir el UserPrincipal sin volver a la BD (excepto la verificación del jti
 * contra sec.tokens).
 *
 * <p>El access token incluye permisos y sucursales para que Angular renderice menús sin
 * round-trips. El refresh token usa solamente jti, sesionId, usuarioId.
 */
public record JwtClaims(
    UUID jti,
    UUID sesionId,
    TipoToken tipo,
    UUID usuarioId,
    String nombreUsuario,
    UUID empresaId,
    UUID sucursalActiva,
    List<String> roles,
    PermissionMap permisos,
    List<UUID> sucursalesHabilitadas,
    LocalDateTime emitidoEn,
    LocalDateTime expiraEn) {}
