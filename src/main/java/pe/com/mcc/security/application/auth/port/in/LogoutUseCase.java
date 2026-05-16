package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

public interface LogoutUseCase {

  /** Logout de la sesión actual: revoca todos los tokens activos de esa sesion_id. */
  void logout(UUID sesionId);

  /** Logout en todos los dispositivos del usuario. */
  void logoutAll(UUID usuarioId);
}
