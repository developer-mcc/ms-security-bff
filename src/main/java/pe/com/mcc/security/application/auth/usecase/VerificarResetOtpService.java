package pe.com.mcc.security.application.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.VerificarResetOtpCommand;
import pe.com.mcc.security.application.auth.port.in.VerificarResetOtpResult;
import pe.com.mcc.security.application.auth.port.in.VerificarResetOtpUseCase;
import pe.com.mcc.security.application.auth.port.out.ResetSesionRepository;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.domain.auth.exception.ResetSesionInvalidaException;
import pe.com.mcc.security.domain.auth.model.EstadoResetSesion;
import pe.com.mcc.security.domain.auth.model.ResetSesion;
import pe.com.mcc.security.domain.otp.exception.OtpExpiradoException;
import pe.com.mcc.security.domain.otp.exception.OtpInvalidoException;
import pe.com.mcc.security.domain.otp.exception.OtpMaxIntentosException;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

/**
 * noRollbackFor: el contador de intentos del OTP debe persistirse aunque la transacción termine con
 * excepción de validación — mismo patrón que VerificarOtpService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificarResetOtpService implements VerificarResetOtpUseCase {

  private final ResetSesionRepository resetSesionRepository;
  private final VerificarOtpUseCase verificarOtp;
  private final Clock clock;

  @Override
  @Transactional(
      noRollbackFor = {
        OtpInvalidoException.class,
        OtpExpiradoException.class,
        OtpMaxIntentosException.class
      })
  public VerificarResetOtpResult verificar(VerificarResetOtpCommand comando) {
    ResetSesion sesion =
        resetSesionRepository
            .buscarPorId(comando.preResetToken())
            .orElseThrow(
                () -> new ResetSesionInvalidaException("Token de reset inválido o no encontrado."));

    if (sesion.estaExpirada(clock.now())) {
      throw new ResetSesionInvalidaException(
          "El token de reset ha expirado. Inicia el proceso nuevamente.");
    }
    if (sesion.estado() != EstadoResetSesion.PENDIENTE) {
      throw new ResetSesionInvalidaException(
          "El OTP ya fue verificado o el token de reset ya fue utilizado.");
    }

    verificarOtp.verificar(
        new VerificarOtpCommand(
            sesion.usuarioId(),
            comando.codigo(),
            PropositoOtp.RESET_PASSWORD,
            comando.direccionIp()));

    resetSesionRepository.guardar(sesion.verificada());

    log.debug("Reset OTP verificado sesion={}", sesion.id());
    return new VerificarResetOtpResult(sesion.id());
  }
}
