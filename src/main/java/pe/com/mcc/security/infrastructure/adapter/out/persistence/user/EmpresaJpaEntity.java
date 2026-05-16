package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.shared.BaseAuditableJpaEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "empresas")
public class EmpresaJpaEntity extends BaseAuditableJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "ruc", nullable = false, unique = true, length = 11)
  private String ruc;

  @Column(name = "razon_social", nullable = false, length = 200)
  private String razonSocial;

  @Column(name = "nombre_comercial", length = 200)
  private String nombreComercial;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "fecha_alta", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @PrePersist
  private void prePersist() {
    if (fechaAlta == null) {
      fechaAlta = LocalDateTime.now();
    }
    if (estado == null) {
      estado = "ACTIVO";
    }
  }
}
