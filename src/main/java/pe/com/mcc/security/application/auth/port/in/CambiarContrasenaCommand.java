package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

public record CambiarContrasenaCommand(UUID usuarioId, String nuevaContrasena) {}
