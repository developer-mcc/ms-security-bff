package pe.com.mcc.security.domain.notification.exception;

import pe.com.mcc.security.domain.user.model.CanalOtp;

public class NotificacionFallidaException extends RuntimeException {

  private final CanalOtp canal;

  public NotificacionFallidaException(CanalOtp canal, Throwable cause) {
    super("Fallo al enviar notificación por canal " + canal, cause);
    this.canal = canal;
  }

  public CanalOtp getCanal() {
    return canal;
  }
}
