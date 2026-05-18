package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;
import pe.com.mcc.security.domain.user.model.CanalOtp;

@NullMarked
public record SolicitarResetRequest(
    @NotBlank @Size(max = 200) String contacto, @NotNull CanalOtp canal) {}
