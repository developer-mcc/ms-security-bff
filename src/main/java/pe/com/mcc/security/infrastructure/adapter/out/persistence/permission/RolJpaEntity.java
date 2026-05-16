package pe.com.mcc.security.infrastructure.adapter.out.persistence.permission;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.shared.BaseAuditableJpaEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "roles")
public class RolJpaEntity extends BaseAuditableJpaEntity {

  @Id
  @Column(name = "id", nullable = false, length = 30)
  private String id;

  @Column(name = "nombre", nullable = false, length = 100)
  private String nombre;

  @Column(name = "descripcion", length = 300)
  private String descripcion;

  @Column(name = "es_sistema", nullable = false)
  private boolean esSistema = true;

  @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PermisoRolRecursoJpaEntity> permisos = new ArrayList<>();
}
