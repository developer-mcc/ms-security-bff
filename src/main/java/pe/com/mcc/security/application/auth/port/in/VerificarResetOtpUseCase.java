package pe.com.mcc.security.application.auth.port.in;

public interface VerificarResetOtpUseCase {

  VerificarResetOtpResult verificar(VerificarResetOtpCommand comando);
}
