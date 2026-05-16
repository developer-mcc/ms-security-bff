package pe.com.mcc.security.infrastructure.tenant;

import java.util.UUID;
import pe.com.mcc.security.domain.tenant.model.TenantInfo;

/**
 * Holder thread-local del tenant resuelto desde el JWT.
 *
 * <p>Lo pobla TenantResolverFilter al entrar la request, lo limpia en finally al salir. El
 * TenantFilterAspect lo lee dentro de cada @Transactional para activar el filtro Hibernate
 * "tenantFilter" sin que los casos de uso conozcan empresa_id explícito.
 *
 * <p>Importante: debe limpiarse al final de cada request (try/finally en el filter) para evitar que
 * un thread del pool herede tenant de un request anterior.
 */
public final class TenantContext {

  private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(TenantInfo info) {
    CONTEXT.set(info);
  }

  public static TenantInfo get() {
    return CONTEXT.get();
  }

  public static UUID empresaId() {
    TenantInfo info = CONTEXT.get();
    return info != null ? info.empresaId() : null;
  }

  public static UUID sucursalId() {
    TenantInfo info = CONTEXT.get();
    return info != null ? info.sucursalId() : null;
  }

  public static boolean tieneTenant() {
    return empresaId() != null;
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
