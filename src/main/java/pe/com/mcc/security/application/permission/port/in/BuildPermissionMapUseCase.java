package pe.com.mcc.security.application.permission.port.in;

import java.util.UUID;
import pe.com.mcc.security.domain.permission.model.PermissionMap;

public interface BuildPermissionMapUseCase {
  PermissionMap build(UUID usuarioId);
}
