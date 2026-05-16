package pe.com.mcc.security.application.user.port.in;

import pe.com.mcc.security.domain.user.model.Usuario;

public interface CrearUsuarioUseCase {
  Usuario crear(CrearUsuarioCommand command);
}
