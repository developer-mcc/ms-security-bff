package pe.com.mcc.security.domain.auth.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Sesión temporal creada tras validar la contraseña cuando MFA está habilitado. Su UUID opaco actúa
 * como preAuthToken: identifica al usuario a lo largo del segundo factor sin exponer un JWT real.
 * TTL corto (5 min) — si expira, el usuario debe iniciar sesión nuevamente.
 */
@NullMarked
public record PreAuthSesion(
    UUID id,
    UUID usuarioId,
    @Nullable UUID empresaId,
    EstadoPreAuthSesion estado,
    @Nullable String ipOrigen,
    LocalDateTime expiraEn) {

  public static PreAuthSesion nueva(
      UUID usuarioId, @Nullable UUID empresaId, @Nullable String ipOrigen, LocalDateTime expiraEn) {
    return new PreAuthSesion(
        UUID.randomUUID(), usuarioId, empresaId, EstadoPreAuthSesion.PENDIENTE, ipOrigen, expiraEn);
  }

  public boolean estaActiva(LocalDateTime ahora) {
    return estado == EstadoPreAuthSesion.PENDIENTE && !ahora.isAfter(expiraEn);
  }

  public PreAuthSesion usada() {
    return new PreAuthSesion(
        id, usuarioId, empresaId, EstadoPreAuthSesion.USADA, ipOrigen, expiraEn);
  }
}
