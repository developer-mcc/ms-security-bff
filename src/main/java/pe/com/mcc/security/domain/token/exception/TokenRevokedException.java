package pe.com.mcc.security.domain.token.exception;

public class TokenRevokedException extends TokenInvalidException {
  public TokenRevokedException() {
    super("Token revocado.");
  }
}
