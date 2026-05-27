package pe.com.mcc.security.application.auth.port.in;

import pe.com.mcc.security.domain.auth.model.AuthenticateResult;

public interface SolicitarMfaUseCase {

  AuthenticateResult.MfaRequerido solicitar(SolicitarMfaCommand command);
}
