package pe.com.mcc.security.infrastructure.adapter.in.web.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CrearUsuarioRequest(
    UUID empresaId,
    @NotBlank @Size(max = 50) String nombreUsuario,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotBlank @Size(min = 8, max = 100) String contrasena,
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @Size(max = 15) String dni,
    @Size(max = 20) String telefono,
    @Size(max = 20) String canalOtpPreferido,
    boolean mfaHabilitado,
    @NotEmpty List<String> rolesIds,
    @NotEmpty List<UUID> sucursalesHabilitadas) {}
