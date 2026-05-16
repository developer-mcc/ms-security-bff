package pe.com.mcc.security.domain.auth.model;

/**
 * Contexto del dispositivo/cliente. Lo extrae el AuthController del HttpServletRequest y lo pasa al
 * caso de uso para persistir en sec.tokens y en la bitácora de auditoría.
 */
public record DispositivoInfo(String direccionIp, String agenteUsuario, String huellaDispositivo) {

  public static DispositivoInfo desconocido() {
    return new DispositivoInfo(null, null, null);
  }
}
