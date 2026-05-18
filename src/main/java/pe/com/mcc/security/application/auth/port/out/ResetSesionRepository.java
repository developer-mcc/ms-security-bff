package pe.com.mcc.security.application.auth.port.out;

import java.util.Optional;
import java.util.UUID;
import pe.com.mcc.security.domain.auth.model.ResetSesion;

public interface ResetSesionRepository {

  void guardar(ResetSesion sesion);

  Optional<ResetSesion> buscarPorId(UUID id);

  void eliminarPendientes(UUID usuarioId);
}
