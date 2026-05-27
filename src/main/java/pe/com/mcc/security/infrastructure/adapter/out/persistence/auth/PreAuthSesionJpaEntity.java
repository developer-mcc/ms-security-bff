package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

/**
 * Token temporal opaco creado al validar la contraseña cuando MFA está activo. Expira en 5 minutos
 * y pasa a estado USADA tras el segundo factor exitoso. No es un JWT: no porta claims, solo
 * identifica al usuario durante el segundo factor.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "pre_auth_sessions")
public class PreAuthSesionJpaEntity implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PostPersist
  @PostLoad
  void markNotNew() {
    isNew = false;
  }

  @Column(name = "usuario_id", nullable = false, updatable = false)
  private UUID usuarioId;

  @Column(name = "empresa_id", updatable = false)
  private UUID empresaId;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  @Column(name = "ip_origen", length = 45, updatable = false)
  private String ipOrigen;

  @Column(name = "creado_en", nullable = false, updatable = false)
  private LocalDateTime creadoEn;

  @Column(name = "expira_en", nullable = false, updatable = false)
  private LocalDateTime expiraEn;

  @PrePersist
  private void prePersist() {
    if (creadoEn == null) {
      creadoEn = LocalDateTime.now();
    }
  }
}
