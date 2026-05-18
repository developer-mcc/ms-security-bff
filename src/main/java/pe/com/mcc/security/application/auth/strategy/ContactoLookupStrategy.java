package pe.com.mcc.security.application.auth.strategy;

import java.util.Optional;
import pe.com.mcc.security.domain.user.model.CanalOtp;
import pe.com.mcc.security.domain.user.model.Usuario;

/** Estrategia para resolver un identificador de contacto en un Usuario según el canal elegido. */
public interface ContactoLookupStrategy {

  boolean soporta(CanalOtp canal);

  Optional<Usuario> buscar(String contacto);
}
