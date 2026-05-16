package pe.com.mcc.security.application.auth.usecase;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.LogoutUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.domain.token.event.TokenRevokedEvent;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;

/**
 * Cerrar sesión = revocar TODOS los tokens (access + refresh) de la sesion_id. El
 * JwtAuthenticationFilter rechazará cualquier request siguiente que use ese par.
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

  private final TokenRepository tokenRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public void logout(UUID sesionId) {
    int revocados = tokenRepository.revokeBySesionId(sesionId, MotivoRevocacion.LOGOUT);
    eventPublisher.publish(
        new TokenRevokedEvent(
            sesionId, null, MotivoRevocacion.LOGOUT, revocados, clock.nowInstant()));
  }

  @Override
  @Transactional
  public void logoutAll(UUID usuarioId) {
    int revocados = tokenRepository.revokeByUsuarioId(usuarioId, MotivoRevocacion.LOGOUT_ALL);
    eventPublisher.publish(
        new TokenRevokedEvent(
            null, usuarioId, MotivoRevocacion.LOGOUT_ALL, revocados, clock.nowInstant()));
  }
}
