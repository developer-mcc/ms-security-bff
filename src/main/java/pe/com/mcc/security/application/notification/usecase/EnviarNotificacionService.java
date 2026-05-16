package pe.com.mcc.security.application.notification.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.com.mcc.security.application.notification.port.in.EnviarNotificacionCommand;
import pe.com.mcc.security.application.notification.port.in.EnviarNotificacionUseCase;
import pe.com.mcc.security.application.notification.port.out.ContactoUsuario;
import pe.com.mcc.security.application.notification.port.out.ContactoUsuarioPort;
import pe.com.mcc.security.application.notification.port.out.NotificationChannel;
import pe.com.mcc.security.domain.notification.exception.NotificacionFallidaException;
import pe.com.mcc.security.domain.notification.model.MensajeNotificacion;
import pe.com.mcc.security.domain.user.model.CanalOtp;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnviarNotificacionService implements EnviarNotificacionUseCase {

  private final List<NotificationChannel> channels;
  private final ContactoUsuarioPort contactoUsuarioPort;

  @Override
  public void enviar(EnviarNotificacionCommand comando) {
    ContactoUsuario contacto =
        contactoUsuarioPort
            .buscarContacto(comando.usuarioId())
            .orElseThrow(
                () ->
                    new NotificacionFallidaException(
                        CanalOtp.EMAIL,
                        new IllegalArgumentException(
                            "Usuario sin datos de contacto: " + comando.usuarioId())));

    CanalOtp canal = comando.canal() != null ? comando.canal() : contacto.canalPreferido();
    MensajeNotificacion mensaje =
        new MensajeNotificacion(
            comando.usuarioId(),
            canal,
            resolverDestinatario(canal, contacto),
            comando.asunto(),
            comando.cuerpo());

    try {
      seleccionar(canal).enviar(mensaje);
    } catch (Exception ex) {
      if (canal != CanalOtp.EMAIL) {
        log.warn(
            "Canal {} falló para usuario {}; reintentando por EMAIL.",
            canal,
            comando.usuarioId(),
            ex);
        MensajeNotificacion fallback =
            new MensajeNotificacion(
                comando.usuarioId(),
                CanalOtp.EMAIL,
                contacto.correo(),
                comando.asunto(),
                comando.cuerpo());
        seleccionar(CanalOtp.EMAIL).enviar(fallback);
      } else {
        throw new NotificacionFallidaException(canal, ex);
      }
    }
  }

  private String resolverDestinatario(CanalOtp canal, ContactoUsuario contacto) {
    if (canal == CanalOtp.EMAIL || contacto.telefono() == null || contacto.telefono().isBlank()) {
      return contacto.correo();
    }
    return contacto.telefono();
  }

  private NotificationChannel seleccionar(CanalOtp canal) {
    return channels.stream()
        .filter(c -> c.soporta(canal))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Sin canal registrado para: " + canal));
  }
}
