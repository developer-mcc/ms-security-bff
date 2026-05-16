package pe.com.mcc.security.application.permission.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.mcc.security.application.permission.port.in.BuildPermissionMapUseCase;
import pe.com.mcc.security.application.permission.port.out.RolRepository;
import pe.com.mcc.security.domain.permission.model.Permiso;
import pe.com.mcc.security.domain.permission.model.PermissionMap;

@Service
@RequiredArgsConstructor
public class BuildPermissionMapService implements BuildPermissionMapUseCase {

  private final RolRepository rolRepository;

  @Override
  public PermissionMap build(UUID usuarioId) {
    List<Permiso> permisos = rolRepository.findPermisosByUsuarioId(usuarioId);
    return PermissionMap.of(permisos);
  }
}
