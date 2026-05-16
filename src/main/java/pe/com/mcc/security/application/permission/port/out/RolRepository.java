package pe.com.mcc.security.application.permission.port.out;

import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.permission.model.Permiso;

public interface RolRepository {

  /** Permisos efectivos del usuario = unión de los permisos de todos sus roles. */
  List<Permiso> findPermisosByUsuarioId(UUID usuarioId);
}
