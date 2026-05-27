package pe.com.mcc.security.domain.auth.model;

import java.util.UUID;
import pe.com.mcc.security.domain.token.model.TokenPair;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/**
 * Resultado sellado de la autenticación por contraseña. TokenEmitido → el usuario no tiene MFA,
 * recibe tokens de acceso directamente. MfaRequerido → el usuario tiene MFA activo, debe completar
 * el segundo factor en POST /auth/login/mfa/verify.
 */
public sealed interface AuthenticateResult {

  record TokenEmitido(TokenPair pair) implements AuthenticateResult {}

  record MfaRequerido(UUID preAuthToken, CanalOtp canal) implements AuthenticateResult {}
}
