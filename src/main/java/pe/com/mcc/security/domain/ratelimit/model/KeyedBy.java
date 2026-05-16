package pe.com.mcc.security.domain.ratelimit.model;

/**
 * Cómo se construye la "clave" que distingue a cada llamador. - IP: por dirección remota (login,
 * recover-password, endpoints anónimos). - USER_ID: por id del usuario autenticado (otp-verify,
 * acciones sensibles post-login). Si no hay usuario autenticado en el momento, el filter cae a IP
 * como fallback.
 */
public enum KeyedBy {
  IP,
  USER_ID
}
