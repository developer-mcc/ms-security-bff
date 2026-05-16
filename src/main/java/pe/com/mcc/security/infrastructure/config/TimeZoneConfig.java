package pe.com.mcc.security.infrastructure.config;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fuente única de verdad para la zona horaria de la aplicación (America/Lima, UTC-5 fijo).
 *
 * <p>Tres garantías en un solo lugar:
 *
 * <ol>
 *   <li>{@code TimeZone.setDefault} — cubre {@code LocalDateTime.now()}, {@code @PrePersist},
 *       librerías de terceros y cualquier código legacy que lea el default del JVM.
 *   <li>{@code java.time.Clock} bean — inyectado en {@code SystemClockAdapter} para que toda la
 *       capa de aplicación use la zona correcta de forma explícita y testeable.
 *   <li>{@code ZoneId} bean — disponible para cualquier componente que necesite convertir {@code
 *       LocalDateTime ↔ Instant} (ej. JWT claims, auditoria).
 * </ol>
 *
 * <p>Las propiedades {@code spring.jackson.time-zone} e {@code hibernate.jdbc.time_zone} en {@code
 * application.yaml} son complementarias: cubren la serialización JSON y el binding JDBC, pero no el
 * default del JVM.
 */
@Configuration
public class TimeZoneConfig {

  public static final ZoneId ZONE_ID = ZoneId.of("America/Lima");

  /** Fuerza el default del JVM en el arranque, antes de cualquier {@code LocalDateTime.now()}. */
  @PostConstruct
  void enforceDefaultTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZONE_ID));
  }

  /**
   * Reloj del sistema fijado a {@code America/Lima}. Se inyecta en {@code SystemClockAdapter}; en
   * tests se sustituye por {@code Clock.fixed(...)} para tiempo determinístico.
   */
  @Bean
  public java.time.Clock systemClock() {
    return java.time.Clock.system(ZONE_ID);
  }

  /**
   * ZoneId disponible para conversiones {@code LocalDateTime ↔ ZonedDateTime ↔ Instant} en
   * adaptadores (JWT, auditoría).
   */
  @Bean
  public ZoneId appZoneId() {
    return ZONE_ID;
  }
}
