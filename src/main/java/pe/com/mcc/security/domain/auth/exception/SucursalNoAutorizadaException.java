package pe.com.mcc.security.domain.auth.exception;

import java.util.UUID;

/**
 * El usuario intenta cambiarse a una sucursal que no está en su lista de sucursales habilitadas
 * (usuarios_sucursales).
 */
public class SucursalNoAutorizadaException extends AuthenticationException {

  public SucursalNoAutorizadaException(UUID sucursalId) {
    super("El usuario no tiene acceso a la sucursal " + sucursalId);
  }
}
