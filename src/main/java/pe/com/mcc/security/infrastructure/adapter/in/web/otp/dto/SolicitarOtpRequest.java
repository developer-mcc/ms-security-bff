package pe.com.mcc.security.infrastructure.adapter.in.web.otp.dto;

import jakarta.validation.constraints.NotNull;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record SolicitarOtpRequest(@NotNull CanalOtp canal, @NotNull PropositoOtp proposito) {}
