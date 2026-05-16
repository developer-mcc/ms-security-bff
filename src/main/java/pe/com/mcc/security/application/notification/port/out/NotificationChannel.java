package pe.com.mcc.security.application.notification.port.out;

import pe.com.mcc.security.domain.notification.model.MensajeNotificacion;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public interface NotificationChannel {

  boolean soporta(CanalOtp canal);

  void enviar(MensajeNotificacion mensaje);
}
