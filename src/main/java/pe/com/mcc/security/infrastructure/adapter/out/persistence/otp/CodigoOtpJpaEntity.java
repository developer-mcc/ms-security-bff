package pe.com.mcc.security.infrastructure.adapter.out.persistence.otp;

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

/**
 * Almacén de OTPs. codigo_hash en BCrypt, nunca el código en claro. La verificación
 * usa @Lock(PESSIMISTIC_WRITE) para evitar condiciones de carrera en intentos concurrentes.
 * OtpCleanupScheduler purga registros expirados/usados.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "sec", name = "codigos_otp")
public class CodigoOtpJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "usuario_id", nullable = false, updatable = false)
  private UUID usuarioId;

  @Column(name = "codigo_hash", nullable = false, length = 255)
  private String codigoHash;

  @Column(name = "canal", nullable = false, length = 20, updatable = false)
  private String canal;

  @Column(name = "proposito", nullable = false, length = 30, updatable = false)
  private String proposito;

  @Column(name = "intentos", nullable = false)
  private int intentos;

  @Column(name = "intentos_maximos", nullable = false, updatable = false)
  private int intentosMaximos = 3;

  @Column(name = "expira_en", nullable = false, updatable = false)
  private LocalDateTime expiraEn;

  @Column(name = "usado_en")
  private LocalDateTime usadoEn;

  @Column(name = "direccion_ip", length = 45, updatable = false)
  private String direccionIp;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @PrePersist
  private void prePersist() {
    if (fechaCreacion == null) {
      fechaCreacion = LocalDateTime.now();
    }
  }
}
