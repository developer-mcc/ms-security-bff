package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;

public record ResetPasswordCommand(UUID resetToken, String nuevaContrasena) {}
