package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record LoginRequest(
    @NotBlank @Size(max = 50) String nombreUsuario, @NotBlank @Size(max = 200) String contrasena) {}
