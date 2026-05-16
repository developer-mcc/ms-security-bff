package pe.com.mcc.security.domain.user.model;

public enum EstadoUsuario {
  ACTIVO,
  BLOQUEADO,
  INACTIVO,
  PENDIENTE_VERIFICACION;

  public boolean puedeAutenticarse() {
    return this == ACTIVO;
  }
}
