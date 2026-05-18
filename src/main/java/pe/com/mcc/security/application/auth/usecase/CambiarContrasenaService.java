package pe.com.mcc.security.application.auth.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaCommand;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaUseCase;
import pe.com.mcc.security.application.auth.port.out.PasswordEncoderPort;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.token.event.TokenRevokedEvent;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;

@Service
@RequiredArgsConstructor
public class CambiarContrasenaService implements CambiarContrasenaUseCase {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public void cambiarContrasena(CambiarContrasenaCommand comando) {
    String hash = passwordEncoder.encode(comando.nuevaContrasena());
    userRepository.actualizarContrasenaHash(comando.usuarioId(), hash, clock.now());

    int revocados =
        tokenRepository.revokeByUsuarioId(comando.usuarioId(), MotivoRevocacion.PASSWORD_CHANGED);

    eventPublisher.publish(
        new TokenRevokedEvent(
            null,
            comando.usuarioId(),
            MotivoRevocacion.PASSWORD_CHANGED,
            revocados,
            clock.nowInstant()));
  }
}
