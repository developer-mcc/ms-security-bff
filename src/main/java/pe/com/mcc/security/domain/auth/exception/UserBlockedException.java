package pe.com.mcc.security.domain.auth.exception;

public class UserBlockedException extends AuthenticationException {
  public UserBlockedException() {
    super("Usuario bloqueado o inactivo.");
  }
}
