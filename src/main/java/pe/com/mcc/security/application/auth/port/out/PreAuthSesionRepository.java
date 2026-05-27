package pe.com.mcc.security.application.auth.port.out;

import java.util.Optional;
import java.util.UUID;
import pe.com.mcc.security.domain.auth.model.PreAuthSesion;

public interface PreAuthSesionRepository {

  void guardar(PreAuthSesion sesion);

  Optional<PreAuthSesion> buscarPorId(UUID id);

  void eliminarPendientesPorUsuario(UUID usuarioId);
}
