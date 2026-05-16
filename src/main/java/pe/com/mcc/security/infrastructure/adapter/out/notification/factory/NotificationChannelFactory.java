package pe.com.mcc.security.infrastructure.adapter.out.notification.factory;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.notification.port.out.NotificationChannel;
import pe.com.mcc.security.domain.user.model.CanalOtp;

/**
 * Convenience wrapper over the {@link NotificationChannel} bean list. {@code
 * EnviarNotificacionService} injects the list directly (DIP), but this factory is available for
 * infrastructure consumers that need single-channel selection by name.
 */
@Component
@RequiredArgsConstructor
public final class NotificationChannelFactory {

  private final List<NotificationChannel> channels;

  public NotificationChannel seleccionar(CanalOtp canal) {
    return channels.stream()
        .filter(c -> c.soporta(canal))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Sin canal registrado para: " + canal));
  }
}
