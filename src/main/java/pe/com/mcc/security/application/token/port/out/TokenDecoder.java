package pe.com.mcc.security.application.token.port.out;

import pe.com.mcc.security.domain.token.model.JwtClaims;

/**
 * Verifica firma, exp y nbf, y retorna los claims tipados. Lanza TokenExpiredException o
 * TokenInvalidException según corresponda.
 */
public interface TokenDecoder {
  JwtClaims decode(String compactJwt);
}
