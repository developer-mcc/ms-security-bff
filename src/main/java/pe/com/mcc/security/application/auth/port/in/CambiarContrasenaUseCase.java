package pe.com.mcc.security.application.auth.port.in;

public interface CambiarContrasenaUseCase {

  /**
   * Hashea {@code nuevaContrasena}, persiste el hash, revoca todos los tokens activos del usuario
   * con motivo {@code PASSWORD_CHANGED} y publica el evento de auditoría. El llamador debe limpiar
   * sus tokens locales y redirigir al login.
   */
  void cambiarContrasena(CambiarContrasenaCommand comando);
}
