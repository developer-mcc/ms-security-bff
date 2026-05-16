package pe.com.mcc.security.application.user.port.in;

import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.user.model.Usuario;

public interface ListUsuariosUseCase {
  List<Usuario> listarPorEmpresa(UUID empresaId);
}
