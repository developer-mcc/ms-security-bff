package pe.com.mcc.security.infrastructure.tenant;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Activa el filtro Hibernate "tenantFilter" en cada llamada a los casos de uso.
 *
 * <p>Se ejecuta DENTRO del @Transactional (Order = 1, después del transactional advice configurado
 * con order=0 en TransactionConfig). Necesita una Session abierta para llamar enableFilter().
 *
 * <p>Si TenantContext.empresaId() es null (login sin autenticar, SUPER_ADMIN multiempresa), NO
 * activa el filtro — la consulta retorna sin restricción de tenant. Esto es la política deseada: -
 * durante /auth/login el usuario aún no tiene tenant resuelto. - SUPER_ADMIN ve todas las empresas
 * a propósito.
 *
 * <p>Pointcut: cualquier método público en pe.com.mcc.security.application..usecase..*
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilterAspect {

  private static final String FILTER_NAME = "tenantFilter";
  private static final String FILTER_PARAM = "tenantId";

  private final EntityManager entityManager;

  @Around("execution(* pe.com.mcc.security.application..usecase..*(..))")
  public Object aplicarTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
    UUID empresaId = TenantContext.empresaId();
    if (empresaId == null) {
      return joinPoint.proceed();
    }

    // enableFilter retorna el Filter para encadenar setParameter; no es necesario
    // retener la referencia: el filtro queda activo en la Session hasta que
    // disableFilter lo apague en el finally de abajo.
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter(FILTER_NAME).setParameter(FILTER_PARAM, empresaId);

    log.trace(
        "tenantFilter ON empresa={} method={}",
        empresaId,
        joinPoint.getSignature().toShortString());

    try {
      return joinPoint.proceed();
    } finally {
      session.disableFilter(FILTER_NAME);
    }
  }
}
