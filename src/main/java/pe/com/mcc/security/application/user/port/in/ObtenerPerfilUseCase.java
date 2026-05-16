package pe.com.mcc.security.application.user.port.in;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import pe.com.mcc.security.domain.user.model.PerfilUsuario;

@NullMarked
public interface ObtenerPerfilUseCase {

  PerfilUsuario obtenerPerfil(UUID usuarioId, @Nullable UUID sucursalActiva);
}
