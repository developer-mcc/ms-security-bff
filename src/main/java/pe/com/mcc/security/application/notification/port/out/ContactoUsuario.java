package pe.com.mcc.security.application.notification.port.out;

import org.jspecify.annotations.Nullable;
import pe.com.mcc.security.domain.user.model.CanalOtp;

public record ContactoUsuario(String correo, @Nullable String telefono, CanalOtp canalPreferido) {}
