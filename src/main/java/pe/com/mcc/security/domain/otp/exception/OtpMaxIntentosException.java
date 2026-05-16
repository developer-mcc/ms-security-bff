package pe.com.mcc.security.domain.otp.exception;

public class OtpMaxIntentosException extends RuntimeException {

  public OtpMaxIntentosException() {
    super("Se agotaron los intentos permitidos para este OTP.");
  }
}
