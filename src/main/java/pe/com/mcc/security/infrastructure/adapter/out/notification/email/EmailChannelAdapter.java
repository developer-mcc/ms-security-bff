package pe.com.mcc.security.infrastructure.adapter.out.notification.email;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.notification.port.out.NotificationChannel;
import pe.com.mcc.security.domain.notification.model.MensajeNotificacion;
import pe.com.mcc.security.domain.user.model.CanalOtp;
import pe.com.mcc.security.infrastructure.config.NotificationProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannelAdapter implements NotificationChannel {

  private static final String INSTANCE = "email-notification";

  private final JavaMailSender mailSender;
  private final NotificationProperties properties;

  @Override
  public boolean soporta(CanalOtp canal) {
    return canal == CanalOtp.EMAIL;
  }

  @Override
  @Retry(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE)
  @Bulkhead(name = INSTANCE)
  public void enviar(MensajeNotificacion mensaje) {
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(properties.getEmail().getFrom());
    mail.setTo(mensaje.destinatario());
    mail.setSubject(mensaje.asunto());
    mail.setText(mensaje.cuerpo());
    mailSender.send(mail);
    log.debug("Email enviado a {} para usuario {}", mensaje.destinatario(), mensaje.usuarioId());
  }
}
