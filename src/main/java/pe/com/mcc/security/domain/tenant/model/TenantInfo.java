package pe.com.mcc.security.domain.tenant.model;

import java.util.UUID;

/**
 * Identidad multi-tenant resuelta a partir del JWT del request. empresaId puede ser null para
 * SUPER_ADMIN (multiempresa).
 */
public record TenantInfo(UUID empresaId, UUID sucursalId) {

  public static TenantInfo empty() {
    return new TenantInfo(null, null);
  }

  public boolean tieneEmpresa() {
    return empresaId != null;
  }
}
