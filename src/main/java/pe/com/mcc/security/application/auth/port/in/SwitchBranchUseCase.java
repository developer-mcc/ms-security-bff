package pe.com.mcc.security.application.auth.port.in;

import pe.com.mcc.security.domain.token.model.TokenPair;

public interface SwitchBranchUseCase {
  TokenPair switchBranch(SwitchBranchCommand command);
}
