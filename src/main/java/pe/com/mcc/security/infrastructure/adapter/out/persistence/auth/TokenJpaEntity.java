package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de JTI emitidos. JwtAuthenticationFilter valida el jti aquí antes de construir el
 * SecurityContext. Un token revocado es rechazado aunque la firma JWT sea válida.
 *
 * <p>Flujo logout: UPDATE tokens SET revocado=TRUE WHERE sesion_id=? AND revocado=FALSE Revoca en
 * una sola sentencia el access y el refresh de la sesión.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "tokens")
public class TokenJpaEntity {

  @Id
  @Column(name = "jti", updatable = false, nullable = false)
  private UUID jti;

  @Column(name = "sesion_id", nullable = false, updatable = false)
  private UUID sesionId;

  @Column(name = "usuario_id", nullable = false, updatable = false)
  private UUID usuarioId;

  @Column(name = "empresa_id", updatable = false)
  private UUID empresaId;

  @Column(name = "sucursal_id", updatable = false)
  private UUID sucursalId;

  @Column(name = "tipo", nullable = false, length = 20, updatable = false)
  private String tipo;

  @Column(name = "jti_padre", updatable = false)
  private UUID jtiPadre;

  @Column(name = "huella_dispositivo", length = 255, updatable = false)
  private String huellaDispositivo;

  @Column(name = "direccion_ip", length = 45, updatable = false)
  private String direccionIp;

  @Column(name = "agente_usuario", length = 500, updatable = false)
  private String agenteUsuario;

  @Column(name = "emitido_en", nullable = false, updatable = false)
  private LocalDateTime emitidoEn;

  @Column(name = "expira_en", nullable = false, updatable = false)
  private LocalDateTime expiraEn;

  @Column(name = "revocado", nullable = false)
  private boolean revocado;

  @Column(name = "revocado_en")
  private LocalDateTime revocadoEn;

  @Column(name = "motivo_revocacion", length = 50)
  private String motivoRevocacion;

  @PrePersist
  private void prePersist() {
    if (emitidoEn == null) {
      emitidoEn = LocalDateTime.now();
    }
  }
}
