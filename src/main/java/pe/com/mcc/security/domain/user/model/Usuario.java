package pe.com.mcc.security.domain.user.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de dominio del usuario autenticable. Inmutable: las transiciones de estado se hacen vía
 * métodos que devuelven una nueva instancia. Esto evita mutaciones accidentales en los casos de
 * uso.
 *
 * <p>empresaId es null para SUPER_ADMIN.
 */
public record Usuario(
    UUID id,
    UUID empresaId,
    String nombreUsuario,
    String correo,
    String contrasenaHash,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    CanalOtp canalOtpPreferido,
    boolean mfaHabilitado,
    EstadoUsuario estado,
    int intentosFallidos,
    LocalDateTime bloqueadoHasta,
    LocalDateTime ultimoAcceso,
    List<String> rolesIds) {

  public static final int MAX_INTENTOS_FALLIDOS = 5;

  /**
   * Estados que NO permiten autenticación (independiente del bloqueo temporal). BLOQUEADO se
   * considera "tiempo de espera" si bloqueadoHasta aún no expira; pasado ese momento, la cuenta
   * queda desbloqueada efectivamente y un login exitoso la pondrá ACTIVO.
   */
  public boolean estaBloqueado(LocalDateTime ahora) {
    if (estado == EstadoUsuario.INACTIVO || estado == EstadoUsuario.PENDIENTE_VERIFICACION) {
      return true;
    }
    if (estado == EstadoUsuario.BLOQUEADO) {
      return bloqueadoHasta == null || bloqueadoHasta.isAfter(ahora);
    }
    return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
  }

  public Usuario conIntentoFallido(LocalDateTime ahora) {
    int nuevos = intentosFallidos + 1;
    boolean bloquear = nuevos >= MAX_INTENTOS_FALLIDOS;
    return new Usuario(
        id,
        empresaId,
        nombreUsuario,
        correo,
        contrasenaHash,
        nombres,
        apellidos,
        dni,
        telefono,
        canalOtpPreferido,
        mfaHabilitado,
        bloquear ? EstadoUsuario.BLOQUEADO : estado,
        nuevos,
        bloquear ? ahora.plusMinutes(15) : bloqueadoHasta,
        ultimoAcceso,
        rolesIds);
  }

  /**
   * Limpia el estado tras un login exitoso: contador a 0, sin bloqueo y, si la cuenta estaba en
   * BLOQUEADO con el tiempo expirado, la pasa a ACTIVO. INACTIVO o PENDIENTE_VERIFICACION no llegan
   * aquí (estaBloqueado los rechaza antes).
   */
  public Usuario conLoginExitoso(LocalDateTime ahora) {
    EstadoUsuario nuevoEstado = (estado == EstadoUsuario.BLOQUEADO) ? EstadoUsuario.ACTIVO : estado;
    return new Usuario(
        id,
        empresaId,
        nombreUsuario,
        correo,
        contrasenaHash,
        nombres,
        apellidos,
        dni,
        telefono,
        canalOtpPreferido,
        mfaHabilitado,
        nuevoEstado,
        0,
        null,
        ahora,
        rolesIds);
  }
}
