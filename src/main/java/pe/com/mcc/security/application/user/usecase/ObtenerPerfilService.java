package pe.com.mcc.security.application.user.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.user.port.in.ObtenerPerfilUseCase;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.PerfilUsuario;
import pe.com.mcc.security.domain.user.model.SucursalUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

@NullMarked
@Service
@RequiredArgsConstructor
public class ObtenerPerfilService implements ObtenerPerfilUseCase {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public PerfilUsuario obtenerPerfil(UUID usuarioId, @Nullable UUID sucursalActiva) {
    Usuario usuario =
        userRepository
            .findById(usuarioId)
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + usuarioId));

    List<SucursalUsuario> sucursales = userRepository.findSucursalesByUsuarioId(usuarioId);

    String sucursalActivaNombre = resolverNombreSucursal(sucursales, sucursalActiva);
    String rol = usuario.rolesIds().isEmpty() ? null : usuario.rolesIds().get(0);

    return new PerfilUsuario(
        usuario.id(),
        usuario.empresaId(),
        usuario.nombreUsuario(),
        usuario.correo(),
        usuario.nombres(),
        usuario.apellidos(),
        usuario.dni(),
        usuario.telefono(),
        usuario.canalOtpPreferido(),
        usuario.mfaHabilitado(),
        rol,
        sucursalActiva,
        sucursalActivaNombre,
        sucursales);
  }

  private static @Nullable String resolverNombreSucursal(
      List<SucursalUsuario> sucursales, @Nullable UUID sucursalActiva) {
    if (sucursalActiva == null) {
      return null;
    }
    return sucursales.stream()
        .filter(s -> s.sucursalId().equals(sucursalActiva))
        .map(SucursalUsuario::nombre)
        .findFirst()
        .orElse(null);
  }
}
