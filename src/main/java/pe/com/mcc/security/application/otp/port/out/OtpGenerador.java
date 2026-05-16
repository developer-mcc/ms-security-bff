package pe.com.mcc.security.application.otp.port.out;

public interface OtpGenerador {

  /** Genera un código OTP numérico de 6 dígitos usando SecureRandom. */
  String generar();
}
