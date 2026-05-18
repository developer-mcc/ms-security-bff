package pe.com.mcc.security.application.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaCommand;
import pe.com.mcc.security.application.auth.port.in.CambiarContrasenaUseCase;
import pe.com.mcc.security.application.auth.port.in.ResetPasswordCommand;
import pe.com.mcc.security.application.auth.port.in.ResetPasswordUseCase;
import pe.com.mcc.security.application.auth.port.out.ResetSesionRepository;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.domain.auth.exception.ResetSesionInvalidaException;
import pe.com.mcc.security.domain.auth.model.EstadoResetSesion;
import pe.com.mcc.security.domain.auth.model.ResetSesion;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

  private final ResetSesionRepository resetSesionRepository;
  private final CambiarContrasenaUseCase cambiarContrasena;
  private final Clock clock;

  @Override
  @Transactional
  public void reset(ResetPasswordCommand comando) {
    ResetSesion sesion =
        resetSesionRepository
            .buscarPorId(comando.resetToken())
            .orElseThrow(
                () -> new ResetSesionInvalidaException("Token de reset inválido o no encontrado."));

    if (sesion.estaExpirada(clock.now())) {
      throw new ResetSesionInvalidaException(
          "El token de reset ha expirado. Inicia el proceso nuevamente.");
    }
    if (sesion.estado() != EstadoResetSesion.VERIFICADO) {
      throw new ResetSesionInvalidaException(
          "El OTP aún no fue verificado. Completa el paso anterior.");
    }

    resetSesionRepository.guardar(sesion.usada());

    cambiarContrasena.cambiarContrasena(
        new CambiarContrasenaCommand(sesion.usuarioId(), comando.nuevaContrasena()));

    log.debug("Contraseña restablecida via reset-flow sesion={}", sesion.id());
  }
}
