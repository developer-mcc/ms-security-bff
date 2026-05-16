package pe.com.mcc.security.application.otp.usecase;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpUseCase;
import pe.com.mcc.security.application.otp.port.out.OtpGenerador;
import pe.com.mcc.security.application.otp.port.out.OtpHasher;
import pe.com.mcc.security.application.otp.port.out.OtpRepository;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.shared.port.out.EventPublisher;
import pe.com.mcc.security.domain.otp.event.OtpSolicitadoEvent;
import pe.com.mcc.security.domain.otp.model.CodigoOtp;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitarOtpService implements SolicitarOtpUseCase {

  private static final int MINUTOS_EXPIRACION = 5;

  private final OtpRepository otpRepository;
  private final OtpGenerador otpGenerador;
  private final OtpHasher otpHasher;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public void solicitar(SolicitarOtpCommand command) {
    otpRepository.invalidarPendientes(command.usuarioId(), command.proposito());

    String codigoPlano = otpGenerador.generar();
    String hash = otpHasher.hashear(codigoPlano);
    LocalDateTime expiraEn = clock.now().plusMinutes(MINUTOS_EXPIRACION);

    CodigoOtp otp =
        CodigoOtp.crear(
            command.usuarioId(),
            hash,
            command.canal(),
            command.proposito(),
            expiraEn,
            command.direccionIp());

    otpRepository.guardar(otp);

    eventPublisher.publish(
        new OtpSolicitadoEvent(
            command.usuarioId(),
            codigoPlano,
            command.canal(),
            command.proposito(),
            command.direccionIp(),
            clock.nowInstant()));

    log.debug(
        "OTP solicitado usuario={} proposito={} canal={}",
        command.usuarioId(),
        command.proposito(),
        command.canal());
  }
}
