package pe.com.mcc.security.domain.auth.model;

import java.util.Objects;

/**
 * Value object: credenciales en claro recibidas del cliente. No se loguea ni se almacena (la
 * contraseña se borra del heap tras el matches).
 */
public record Credentials(String nombreUsuario, String contrasena) {

  public Credentials {
    Objects.requireNonNull(nombreUsuario, "nombreUsuario");
    Objects.requireNonNull(contrasena, "contrasena");
    if (nombreUsuario.isBlank()) {
      throw new IllegalArgumentException("nombreUsuario vacío");
    }
    if (contrasena.isBlank()) {
      throw new IllegalArgumentException("contrasena vacía");
    }
  }

  @Override
  public String toString() {
    return "Credentials{nombreUsuario='" + nombreUsuario + "', contrasena=***}";
  }
}
