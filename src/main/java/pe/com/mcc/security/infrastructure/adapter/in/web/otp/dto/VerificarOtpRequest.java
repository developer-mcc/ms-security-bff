package pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

public record VerificarOtpRequest(
    @NotBlank @Pattern(regexp = "\\d{6}", message = "El código debe tener exactamente 6 dígitos")
        String codigo,
    @NotNull PropositoOtp proposito) {}
