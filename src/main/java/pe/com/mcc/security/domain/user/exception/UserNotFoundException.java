package pe.com.mcc.security.domain.user.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(String nombreUsuario) {
    super("Usuario no encontrado: " + nombreUsuario);
  }
}
