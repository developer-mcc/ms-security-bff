package pe.com.mcc.security.domain.otp.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/**
 * Entidad de dominio OTP. Inmutable: las transiciones de estado retornan una nueva instancia. El
 * código en claro nunca se almacena — solo codigoHash (BCrypt).
 */
public record CodigoOtp(
    UUID id,
    UUID usuarioId,
    String codigoHash,
    CanalOtp canal,
    PropositoOtp proposito,
    int intentos,
    LocalDateTime expiraEn,
    LocalDateTime usadoEn,
    String direccionIp) {

  public static final int INTENTOS_MAXIMOS = 3;

  public static CodigoOtp crear(
      UUID usuarioId,
      String codigoHash,
      CanalOtp canal,
      PropositoOtp proposito,
      LocalDateTime expiraEn,
      String direccionIp) {
    Objects.requireNonNull(usuarioId, "usuarioId es requerido");
    Objects.requireNonNull(codigoHash, "codigoHash es requerido");
    Objects.requireNonNull(canal, "canal es requerido");
    Objects.requireNonNull(proposito, "proposito es requerido");
    Objects.requireNonNull(expiraEn, "expiraEn es requerido");
    return new CodigoOtp(
        null, usuarioId, codigoHash, canal, proposito, 0, expiraEn, null, direccionIp);
  }

  @SuppressWarnings("PMD.ExcessiveParameterList")
  public static CodigoOtp reconstituir(
      UUID id,
      UUID usuarioId,
      String codigoHash,
      CanalOtp canal,
      PropositoOtp proposito,
      int intentos,
      LocalDateTime expiraEn,
      LocalDateTime usadoEn,
      String direccionIp) {
    return new CodigoOtp(
        id, usuarioId, codigoHash, canal, proposito, intentos, expiraEn, usadoEn, direccionIp);
  }

  public boolean estaExpirado(LocalDateTime ahora) {
    return ahora.isAfter(expiraEn);
  }

  public boolean estaUsado() {
    return usadoEn != null;
  }

  public boolean haAlcanzadoMaxIntentos() {
    return intentos >= INTENTOS_MAXIMOS;
  }

  /** Retorna una nueva instancia con el contador de intentos incrementado. */
  public CodigoOtp conIntentoFallido() {
    return new CodigoOtp(
        id, usuarioId, codigoHash, canal, proposito, intentos + 1, expiraEn, usadoEn, direccionIp);
  }

  /** Retorna una nueva instancia marcada como usada. */
  public CodigoOtp marcadoComoUsado(LocalDateTime ahora) {
    return new CodigoOtp(
        id, usuarioId, codigoHash, canal, proposito, intentos, expiraEn, ahora, direccionIp);
  }
}
