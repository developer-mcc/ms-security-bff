package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;

/** Respuesta 202 cuando el login requiere segundo factor. */
@NullMarked
public record MfaRequeridoResponse(UUID preAuthToken, String canal) {}
