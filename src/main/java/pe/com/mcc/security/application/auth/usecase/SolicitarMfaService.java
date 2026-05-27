package pe.com.mcc.security.application.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.SolicitarMfaCommand;
import pe.com.mcc.security.application.auth.port.in.SolicitarMfaUseCase;
import pe.com.mcc.security.application.auth.port.out.PreAuthSesionRepository;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpUseCase;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.domain.auth.model.AuthenticateResult;
import pe.com.mcc.security.domain.auth.model.PreAuthSesion;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

/**
 * Orquesta el primer paso del flujo MFA: crea la PreAuthSesion y dispara el envío del OTP al canal
 * preferido del usuario. El canal se resuelve desde el perfil persisitido — sin input del cliente —
 * porque el usuario aún no está autenticado en este punto.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitarMfaService implements SolicitarMfaUseCase {

  private static final int MINUTOS_EXPIRACION = 5;

  private final PreAuthSesionRepository preAuthSesionRepository;
  private final SolicitarOtpUseCase solicitarOtp;
  private final Clock clock;

  @Override
  @Transactional
  public AuthenticateResult.MfaRequerido solicitar(SolicitarMfaCommand command) {
    preAuthSesionRepository.eliminarPendientesPorUsuario(command.usuario().id());

    PreAuthSesion sesion =
        PreAuthSesion.nueva(
            command.usuario().id(),
            command.usuario().empresaId(),
            command.direccionIp(),
            clock.now().plusMinutes(MINUTOS_EXPIRACION));

    preAuthSesionRepository.guardar(sesion);

    solicitarOtp.solicitar(
        new SolicitarOtpCommand(
            command.usuario().id(),
            command.usuario().canalOtpPreferido(),
            PropositoOtp.LOGIN_2FA,
            command.direccionIp()));

    log.debug(
        "MFA solicitado usuario={} canal={}",
        command.usuario().id(),
        command.usuario().canalOtpPreferido());

    return new AuthenticateResult.MfaRequerido(sesion.id(), command.usuario().canalOtpPreferido());
  }
}
