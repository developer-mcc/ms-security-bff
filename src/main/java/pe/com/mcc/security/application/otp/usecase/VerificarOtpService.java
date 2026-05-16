package pe.com.mcc.security.application.otp.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.VerificarOtpUseCase;
import pe.com.mcc.security.application.otp.port.out.OtpHasher;
import pe.com.mcc.security.application.otp.port.out.OtpRepository;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.domain.otp.event.OtpFallidoEvent;
import pe.com.mcc.security.domain.otp.event.OtpVerificadoEvent;
import pe.com.mcc.security.domain.otp.exception.OtpExpiradoException;
import pe.com.mcc.security.domain.otp.exception.OtpInvalidoException;
import pe.com.mcc.security.domain.otp.exception.OtpMaxIntentosException;
import pe.com.mcc.security.domain.otp.model.CodigoOtp;

/**
 * Verificación con @Lock(PESSIMISTIC_WRITE) en el repository para evitar condiciones de carrera.
 *
 * <p>noRollbackFor: el incremento del contador de intentos fallidos debe persistirse aunque la
 * transacción termine con excepción — mismo patrón que RegistrarIntentoFallidoService en auth.
 * REQUIRES_NEW no se usa aquí porque ya tenemos PESSIMISTIC_WRITE sobre la misma fila y el deadlock
 * sería inevitable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificarOtpService implements VerificarOtpUseCase {

  private final OtpRepository otpRepository;
  private final OtpHasher otpHasher;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional(
      noRollbackFor = {
        OtpInvalidoException.class,
        OtpExpiradoException.class,
        OtpMaxIntentosException.class
      })
  public void verificar(VerificarOtpCommand command) {
    CodigoOtp otp =
        otpRepository
            .buscarParaVerificar(command.usuarioId(), command.proposito())
            .orElseThrow(
                () -> {
                  publicarFallo(command, "OTP_NO_ENCONTRADO");
                  return new OtpInvalidoException();
                });

    if (otp.estaExpirado(clock.now())) {
      publicarFallo(command, "OTP_EXPIRADO");
      throw new OtpExpiradoException();
    }

    if (otp.haAlcanzadoMaxIntentos()) {
      publicarFallo(command, "MAX_INTENTOS_ALCANZADOS");
      throw new OtpMaxIntentosException();
    }

    if (!otpHasher.verificar(command.codigo(), otp.codigoHash())) {
      otpRepository.guardar(otp.conIntentoFallido());
      publicarFallo(command, "CODIGO_INVALIDO");
      throw new OtpInvalidoException();
    }

    otpRepository.guardar(otp.marcadoComoUsado(clock.now()));

    eventPublisher.publish(
        new OtpVerificadoEvent(
            command.usuarioId(), command.proposito(), command.direccionIp(), clock.nowInstant()));

    log.debug("OTP verificado usuario={} proposito={}", command.usuarioId(), command.proposito());
  }

  private void publicarFallo(VerificarOtpCommand command, String motivo) {
    eventPublisher.publish(
        new OtpFallidoEvent(
            command.usuarioId(),
            command.proposito(),
            motivo,
            command.direccionIp(),
            clock.nowInstant()));
  }
}
