package pe.com.mcc.security.application.token.port.in;

import pe.com.mcc.security.domain.token.model.TokenPair;

public interface IssueTokenUseCase {
  TokenPair issuePair(IssueTokenPairCommand command);
}
