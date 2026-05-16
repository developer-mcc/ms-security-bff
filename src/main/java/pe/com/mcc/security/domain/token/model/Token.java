package pe.com.mcc.security.domain.token.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Token persistido en sec.tokens (sin el JWT serializado — eso es responsabilidad del adapter). */
public record Token(
    UUID jti,
    UUID sesionId,
    UUID usuarioId,
    UUID empresaId,
    UUID sucursalId,
    TipoToken tipo,
    UUID jtiPadre,
    String huellaDispositivo,
    String direccionIp,
    String agenteUsuario,
    LocalDateTime emitidoEn,
    LocalDateTime expiraEn,
    boolean revocado,
    LocalDateTime revocadoEn,
    MotivoRevocacion motivoRevocacion) {
  public boolean estaActivo(LocalDateTime ahora) {
    return !revocado && expiraEn.isAfter(ahora);
  }
}
