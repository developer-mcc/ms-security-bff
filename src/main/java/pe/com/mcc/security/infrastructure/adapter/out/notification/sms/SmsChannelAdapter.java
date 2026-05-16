package pe.com.mcc.security.infrastructure.adapter.out.notification.sms;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.notification.port.out.NotificationChannel;
import pe.com.mcc.security.domain.notification.model.MensajeNotificacion;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/**
 * SMS gateway stub (Twilio / Vonage not yet integrated). Throws to trigger the email fallback in
 * {@code EnviarNotificacionService}. Add {@code @Retry} and configure {@code retryExceptions} when
 * the real gateway is wired.
 */
@Component
public class SmsChannelAdapter implements NotificationChannel {

  private static final String INSTANCE = "sms-notification";

  @Override
  public boolean soporta(CanalOtp canal) {
    return canal == CanalOtp.SMS;
  }

  @Override
  @CircuitBreaker(name = INSTANCE)
  @Bulkhead(name = INSTANCE)
  public void enviar(MensajeNotificacion mensaje) {
    throw new UnsupportedOperationException("SMS gateway not yet integrated");
  }
}
