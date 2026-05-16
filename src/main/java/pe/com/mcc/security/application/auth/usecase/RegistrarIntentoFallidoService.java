package pe.com.mcc.security.application.auth.usecase;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.RegistrarIntentoFallidoUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Persiste el intento fallido en SU PROPIA transacción (REQUIRES_NEW). Aunque AuthenticateService
 * haga rollback al lanzar InvalidCredentialsException, esta transacción ya commitó y el contador
 * queda actualizado.
 *
 * <p>Si el contador llega a Usuario.MAX_INTENTOS_FALLIDOS (5), la entidad Usuario pasa a
 * estado=BLOQUEADO con bloqueado_hasta = now + 15min — el siguiente intento será rechazado con
 * UserBlockedException antes de validar password.
 */
@Service
@RequiredArgsConstructor
public class RegistrarIntentoFallidoService implements RegistrarIntentoFallidoUseCase {

  private final UserRepository userRepository;
  private final Clock clock;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarFallo(UUID usuarioId) {
    Usuario actual =
        userRepository
            .findById(usuarioId)
            .orElseThrow(() -> new IllegalStateException("Usuario inexistente: " + usuarioId));
    userRepository.save(actual.conIntentoFallido(clock.now()));
  }
}
