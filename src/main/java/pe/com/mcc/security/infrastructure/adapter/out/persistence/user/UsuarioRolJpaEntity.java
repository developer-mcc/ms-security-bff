package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.mcc.security.infrastructure.adapter.out.persistence.permission.RolJpaEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "usuarios_roles")
public class UsuarioRolJpaEntity {

  @EmbeddedId private UsuarioRolId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("usuarioId")
  @JoinColumn(name = "usuario_id")
  private UsuarioJpaEntity usuario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("rolId")
  @JoinColumn(name = "rol_id")
  private RolJpaEntity rol;

  @Column(name = "asignado_por", nullable = false, length = 50)
  private String asignadoPor;

  @Column(name = "asignado_en", nullable = false, updatable = false)
  private LocalDateTime asignadoEn;

  @PrePersist
  private void prePersist() {
    if (asignadoEn == null) {
      asignadoEn = LocalDateTime.now();
    }
    if (asignadoPor == null) {
      asignadoPor = "SYSTEM";
    }
  }
}
