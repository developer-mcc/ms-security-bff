package pe.com.mcc.security.infrastructure.adapter.out.persistence.shared;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/**
 * Mapped superclass para entidades multi-tenant con empresa_id NOT NULL.
 *
 * <p>Aplica el filtro Hibernate "tenantFilter" — el TenantFilterAspect lo activa dentro de
 * cada @Transactional leyendo el TenantContext.
 *
 * <p>El @FilterDef del filtro está declarado en UsuarioJpaEntity (no aquí, porque
 * un @MappedSuperclass no procesado por Hibernate no registra sus @FilterDef).
 *
 * <p>No se usa para UsuarioJpaEntity (empresa_id es nullable: SUPER_ADMIN). Sí para entidades de
 * negocio que requieran tenant obligatorio (productos, ventas, etc.).
 */
@Getter
@Setter
@MappedSuperclass
@Filter(name = "tenantFilter", condition = "empresa_id = :tenantId")
public abstract class BaseTenantJpaEntity extends BaseAuditableJpaEntity {

  @Column(name = "empresa_id", nullable = false, updatable = false)
  private UUID empresaId;
}
