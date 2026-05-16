package pe.com.mcc.security.application.otp.port.in;

public interface SolicitarOtpUseCase {

  void solicitar(SolicitarOtpCommand command);
}
