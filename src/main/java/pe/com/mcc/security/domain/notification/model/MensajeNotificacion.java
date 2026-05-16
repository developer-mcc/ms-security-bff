package pe.com.mcc.security.domain.notification.model;

import java.util.UUID;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record MensajeNotificacion(
    UUID usuarioId, CanalOtp canal, String destinatario, String asunto, String cuerpo) {}
