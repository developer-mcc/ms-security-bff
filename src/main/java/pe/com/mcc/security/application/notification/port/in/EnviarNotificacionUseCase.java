package pe.com.mcc.security.application.notification.port.in;

public interface EnviarNotificacionUseCase {

  void enviar(EnviarNotificacionCommand comando);
}
