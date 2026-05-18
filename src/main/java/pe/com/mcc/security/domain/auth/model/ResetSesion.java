package pe.com.mcc.security.domain.auth.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResetSesion(
    UUID id, UUID usuarioId, EstadoResetSesion estado, LocalDateTime expiraEn) {

  public static ResetSesion nueva(UUID usuarioId, LocalDateTime expiraEn) {
    return new ResetSesion(UUID.randomUUID(), usuarioId, EstadoResetSesion.PENDIENTE, expiraEn);
  }

  public boolean estaExpirada(LocalDateTime ahora) {
    return ahora.isAfter(expiraEn);
  }

  public ResetSesion verificada() {
    return new ResetSesion(id, usuarioId, EstadoResetSesion.VERIFICADO, expiraEn);
  }

  public ResetSesion usada() {
    return new ResetSesion(id, usuarioId, EstadoResetSesion.USADO, expiraEn);
  }
}
