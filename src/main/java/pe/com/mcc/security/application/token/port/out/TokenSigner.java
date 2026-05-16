package pe.com.mcc.security.application.token.port.out;

import pe.com.mcc.security.domain.token.model.JwtClaims;

/**
 * Firma claims y devuelve el JWT compacto. Contrato pequeño (ISP): el firmador no necesita
 * decodificar.
 */
public interface TokenSigner {
  String sign(JwtClaims claims);
}
