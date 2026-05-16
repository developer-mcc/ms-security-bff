package pe.com.mcc.security.domain.otp.exception;

public class OtpExpiradoException extends RuntimeException {

  public OtpExpiradoException() {
    super("El código OTP ha expirado. Solicita uno nuevo.");
  }
}
