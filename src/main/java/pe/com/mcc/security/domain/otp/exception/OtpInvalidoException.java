package pe.com.mcc.security.domain.otp.exception;

public class OtpInvalidoException extends RuntimeException {

  public OtpInvalidoException() {
    super("El código OTP ingresado no es válido.");
  }
}
