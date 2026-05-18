package pe.com.mcc.security.application.auth.port.in;

public interface ResetPasswordUseCase {

  void reset(ResetPasswordCommand comando);
}
