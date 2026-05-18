package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record VerificarResetOtpRequest(
    @NotNull UUID preResetToken, @NotBlank @Size(min = 6, max = 6) String codigo) {}
