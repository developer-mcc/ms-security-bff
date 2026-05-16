package pe.com.mcc.security.application.notification.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ContactoUsuarioPort {

  Optional<ContactoUsuario> buscarContacto(UUID usuarioId);
}
