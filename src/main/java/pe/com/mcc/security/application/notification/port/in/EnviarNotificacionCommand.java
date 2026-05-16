package pe.com.mcc.security.application.notification.port.in;

import java.util.UUID;
import org.jspecify.annotations.Nullable;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/** {@code canal} null means: use the user's preferred channel from their profile. */
public record EnviarNotificacionCommand(
    UUID usuarioId, @Nullable CanalOtp canal, String asunto, String cuerpo) {}
