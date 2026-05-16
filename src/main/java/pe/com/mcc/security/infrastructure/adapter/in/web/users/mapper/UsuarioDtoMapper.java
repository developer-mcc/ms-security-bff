package pe.com.mcc.security.infrastructure.adapter.in.web.users.mapper;

import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.user.port.in.CrearUsuarioCommand;
import pe.com.mcc.security.domain.user.model.CanalOtp;
import pe.com.mcc.security.domain.user.model.Usuario;
import pe.com.mcc.security.infrastructure.adapter.in.web.users.dto.CrearUsuarioRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.users.dto.UsuarioResponse;

@Component
public class UsuarioDtoMapper {

  public CrearUsuarioCommand toCommand(CrearUsuarioRequest body) {
    return new CrearUsuarioCommand(
        body.empresaId(),
        body.nombreUsuario(),
        body.correo(),
        body.contrasena(),
        body.nombres(),
        body.apellidos(),
        body.dni(),
        body.telefono(),
        body.canalOtpPreferido() != null
            ? CanalOtp.valueOf(body.canalOtpPreferido())
            : CanalOtp.EMAIL,
        body.mfaHabilitado(),
        body.rolesIds(),
        body.sucursalesHabilitadas());
  }

  public UsuarioResponse toResponse(Usuario u) {
    return new UsuarioResponse(
        u.id(),
        u.empresaId(),
        u.nombreUsuario(),
        u.correo(),
        u.nombres(),
        u.apellidos(),
        u.estado().name(),
        u.mfaHabilitado(),
        u.rolesIds());
  }
}
