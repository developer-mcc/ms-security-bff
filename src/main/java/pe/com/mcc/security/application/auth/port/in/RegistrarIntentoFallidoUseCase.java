package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

/**
 * Caso de uso interno: incrementa intentos_fallidos del usuario y, si supera el umbral, lo deja
 * BLOQUEADO con bloqueado_hasta = now + 15min.
 *
 * <p>Existe como UseCase separado para correr en Propagation.REQUIRES_NEW: el flujo padre
 * (AuthenticateService) lanza InvalidCredentialsException después, lo que rollback su transacción;
 * sin esta transacción independiente, el incremento nunca se persistiría.
 */
public interface RegistrarIntentoFallidoUseCase {
  void registrarFallo(UUID usuarioId);
}
