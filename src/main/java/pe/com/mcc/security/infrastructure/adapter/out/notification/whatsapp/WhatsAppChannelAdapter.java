package pe.com.mcc.security.infrastructure.adapter.out.notification.whatsapp;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.notification.port.out.NotificationChannel;
import pe.com.mcc.security.domain.notification.model.MensajeNotificacion;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/**
 * WhatsApp Business API stub (Meta Cloud API not yet integrated). Throws to trigger the email
 * fallback in {@code EnviarNotificacionService}. Add {@code @Retry} and configure {@code
 * retryExceptions} when the real gateway is wired.
 */
@Component
public class WhatsAppChannelAdapter implements NotificationChannel {

  private static final String INSTANCE = "whatsapp-notification";

  @Override
  public boolean soporta(CanalOtp canal) {
    return canal == CanalOtp.WHATSAPP;
  }

  @Override
  @CircuitBreaker(name = INSTANCE)
  @Bulkhead(name = INSTANCE)
  public void enviar(MensajeNotificacion mensaje) {
    throw new UnsupportedOperationException("WhatsApp gateway not yet integrated");
  }
}
