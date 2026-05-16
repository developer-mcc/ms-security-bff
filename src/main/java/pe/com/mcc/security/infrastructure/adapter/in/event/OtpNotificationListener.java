package pe.com.mcc.security.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.mcc.security.application.notification.port.in.EnviarNotificacionCommand;
import pe.com.mcc.security.application.notification.port.in.EnviarNotificacionUseCase;
import pe.com.mcc.security.domain.notification.exception.NotificacionFallidaException;
import pe.com.mcc.security.domain.otp.event.OtpSolicitadoEvent;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.infrastructure.config.AsyncConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpNotificationListener {

  private final EnviarNotificacionUseCase enviarNotificacion;

  /**
   * Fires AFTER the OTP transaction commits — guarantees the code is persisted before we send it to
   * the user.
   */
  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOtpSolicitado(OtpSolicitadoEvent event) {
    try {
      enviarNotificacion.enviar(
          new EnviarNotificacionCommand(
              event.usuarioId(),
              event.canal(),
              buildAsunto(event.proposito()),
              buildCuerpo(event.codigoPlano(), event.proposito())));
    } catch (NotificacionFallidaException ex) {
      log.error(
          "No se pudo notificar OTP al usuario {} por canal {}: {}",
          event.usuarioId(),
          event.canal(),
          ex.getMessage());
    }
  }

  private static String buildAsunto(PropositoOtp proposito) {
    return switch (proposito) {
      case LOGIN_2FA -> "Código de verificación de dos factores";
      case RESET_PASSWORD -> "Código para restablecer tu contraseña";
    };
  }

  private static String buildCuerpo(String codigoPlano, PropositoOtp proposito) {
    return switch (proposito) {
      case LOGIN_2FA ->
          "Tu código de verificación es: "
              + codigoPlano
              + ". Válido por 5 minutos. No lo compartas con nadie.";
      case RESET_PASSWORD ->
          "Tu código para restablecer la contraseña es: " + codigoPlano + ". Válido por 5 minutos.";
    };
  }
}
