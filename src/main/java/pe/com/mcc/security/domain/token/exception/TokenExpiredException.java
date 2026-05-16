package pe.com.mcc.security.domain.token.exception;

public class TokenExpiredException extends TokenInvalidException {
  public TokenExpiredException() {
    super("Token expirado.");
  }
}
