package pe.com.mcc.security.infrastructure.adapter.out.persistence.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.shared.BaseAuditableJpaEntity;

/**
 * Modelo 3D: rol x recurso x (acciones[], alcance). acciones es un array PostgreSQL (text[])
 * mapeado con @JdbcTypeCode(SqlTypes.ARRAY). alcance: OWN_BRANCH | ALL_BRANCHES. SUPER_ADMIN se
 * almacena con recurso='*' (comodín) evaluado en ThreeDPermissionEvaluator.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "permisos_rol_recurso")
public class PermisoRolRecursoJpaEntity extends BaseAuditableJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rol_id", nullable = false)
  private RolJpaEntity rol;

  @Column(name = "recurso", nullable = false, length = 50)
  private String recurso;

  @Column(name = "acciones", nullable = false, columnDefinition = "text[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private String[] acciones;

  @Column(name = "alcance", nullable = false, length = 20)
  private String alcance;
}
