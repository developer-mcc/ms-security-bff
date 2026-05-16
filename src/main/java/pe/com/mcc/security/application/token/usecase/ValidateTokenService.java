package pe.com.mcc.security.application.token.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.token.port.in.ValidateTokenUseCase;
import pe.com.mcc.security.application.token.port.out.TokenDecoder;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.domain.token.exception.TokenExpiredException;
import pe.com.mcc.security.domain.token.exception.TokenInvalidException;
import pe.com.mcc.security.domain.token.exception.TokenRevokedException;
import pe.com.mcc.security.domain.token.model.JwtClaims;
import pe.com.mcc.security.domain.token.model.Token;

/**
 * Valida un JWT en dos pasos: 1) firma + exp via TokenDecoder. 2) jti contra sec.tokens (debe
 * existir y estar activo). Si cualquiera falla, lanza la excepción correspondiente del dominio.
 */
@Service
@RequiredArgsConstructor
public class ValidateTokenService implements ValidateTokenUseCase {

  private final TokenDecoder tokenDecoder;
  private final TokenRepository tokenRepository;
  private final Clock clock;

  @Override
  @Transactional(readOnly = true)
  public JwtClaims validate(String compactJwt) {
    JwtClaims claims = tokenDecoder.decode(compactJwt);

    Token token =
        tokenRepository
            .findByJti(claims.jti())
            .orElseThrow(() -> new TokenInvalidException("jti no registrado"));

    if (token.revocado()) {
      throw new TokenRevokedException();
    }
    if (!token.expiraEn().isAfter(clock.now())) {
      throw new TokenExpiredException();
    }

    return claims;
  }
}
