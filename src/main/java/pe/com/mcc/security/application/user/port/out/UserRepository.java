package pe.com.mcc.security.application.user.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

public interface UserRepository {

  Optional<Usuario> findByNombreUsuario(String nombreUsuario);

  Optional<Usuario> findById(UUID usuarioId);

  /** Lista todos los usuarios de una empresa. */
  List<Usuario> findByEmpresaId(UUID empresaId);

  /** Crea un nuevo usuario con sus roles y sucursales asignados. */
  Usuario create(Usuario usuario, List<UUID> sucursalesHabilitadas);

  /** Persiste cambios derivados del flujo de auth (intentos fallidos, ultimoAcceso, estado). */
  void save(Usuario usuario);

  /** Sucursales habilitadas para el usuario, marcando cuál es la predeterminada. */
  List<SucursalUsuario> findSucursalesByUsuarioId(UUID usuarioId);
}
