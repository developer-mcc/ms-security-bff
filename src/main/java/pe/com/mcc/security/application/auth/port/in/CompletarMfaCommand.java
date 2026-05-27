package pe.com.mcc.security.application.auth.port.in;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import pe.com.mcc.security.domain.auth.model.DispositivoInfo;

@NullMarked
public record CompletarMfaCommand(UUID preAuthToken, String codigo, DispositivoInfo dispositivo) {}
