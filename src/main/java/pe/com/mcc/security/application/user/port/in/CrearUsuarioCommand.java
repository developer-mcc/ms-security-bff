package pe.com.mcc.security.application.user.port.in;

import java.util.List;
import java.util.UUID;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record CrearUsuarioCommand(
    UUID empresaId,
    String nombreUsuario,
    String correo,
    String contrasena,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    CanalOtp canalOtpPreferido,
    boolean mfaHabilitado,
    List<String> rolesIds,
    List<UUID> sucursalesHabilitadas) {}
