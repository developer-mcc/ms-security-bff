package pe.com.mcc.security.application.auth.port.in;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import pe.com.mcc.security.domain.user.model.Usuario;

@NullMarked
public record SolicitarMfaCommand(Usuario usuario, @Nullable String direccionIp) {}
