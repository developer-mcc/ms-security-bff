package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.shared.BaseAuditableJpaEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "sucursales")
@Filter(name = "tenantFilter", condition = "empresa_id = :tenantId")
public class SucursalJpaEntity extends BaseAuditableJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
  private EmpresaJpaEntity empresa;

  @Column(name = "codigo", nullable = false, length = 20)
  private String codigo;

  @Column(name = "nombre", nullable = false, length = 150)
  private String nombre;

  @Column(name = "direccion", length = 300)
  private String direccion;

  @Column(name = "distrito", length = 100)
  private String distrito;

  @Column(name = "provincia", length = 100)
  private String provincia;

  @Column(name = "departamento", length = 100)
  private String departamento;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "es_principal", nullable = false)
  private boolean esPrincipal;

  @PrePersist
  private void prePersist() {
    if (estado == null) {
      estado = "ACTIVO";
    }
  }
}
