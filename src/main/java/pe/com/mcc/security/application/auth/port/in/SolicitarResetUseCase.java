package pe.com.mcc.security.application.auth.port.in;

public interface SolicitarResetUseCase {

  SolicitarResetResult solicitar(SolicitarResetCommand comando);
}
