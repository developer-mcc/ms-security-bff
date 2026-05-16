package pe.com.mcc.security.application.token.port.in;

import pe.com.mcc.security.domain.token.model.JwtClaims;

/**
 * Verifica firma + exp del JWT y consulta sec.tokens (jti debe existir y NO estar revocado).
 * Devuelve los claims tipados — el JwtAuthenticationFilter los usa para construir el UserPrincipal.
 */
public interface ValidateTokenUseCase {
  JwtClaims validate(String compactJwt);
}
