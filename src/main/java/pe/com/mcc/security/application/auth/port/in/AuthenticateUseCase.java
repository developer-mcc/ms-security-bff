package pe.com.mcc.security.application.auth.port.in;

import pe.com.mcc.security.domain.token.model.TokenPair;

public interface AuthenticateUseCase {
  TokenPair authenticate(AuthenticateCommand command);
}
