package pe.com.mcc.security.domain.auth.exception;

public class InvalidCredentialsException extends AuthenticationException {
  public InvalidCredentialsException() {
    super("Credenciales inválidas.");
  }
}
