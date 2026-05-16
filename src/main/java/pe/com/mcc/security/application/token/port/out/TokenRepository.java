package pe.com.mcc.security.application.token.port.out;

import java.util.Optional;
import java.util.UUID;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;
import pe.com.mcc.security.domain.token.model.Token;

public interface TokenRepository {

  void save(Token token);

  Optional<Token> findByJti(UUID jti);

  /** Revoca todos los tokens activos de una sesión (logout single-device). */
  int revokeBySesionId(UUID sesionId, MotivoRevocacion motivo);

  /** Revoca todos los tokens activos de un usuario (logout-all-devices, password change). */
  int revokeByUsuarioId(UUID usuarioId, MotivoRevocacion motivo);
}
