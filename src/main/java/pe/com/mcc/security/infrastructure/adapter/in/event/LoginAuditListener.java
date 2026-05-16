package pe.com.mcc.security.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.audit.port.in.RecordAuditUseCase;
import pe.com.mcc.security.domain.audit.model.AccionAuditoria;
import pe.com.mcc.security.domain.audit.model.AuditEntry;
import pe.com.mcc.security.domain.auth.event.LoginFailedEvent;
import pe.com.mcc.security.domain.auth.event.LoginSuccessEvent;
import pe.com.mcc.security.infrastructure.config.AsyncConfig;

/**
 * Escucha eventos de login y los persiste en sec.bitacora_auditoria.
 *
 * <p>Ambos listeners corren en el executor "securityEventsExecutor" — fuera del hilo del request.
 * RecordAuditService usa Propagation.REQUIRES_NEW, así un LoginFailedEvent (cuyo flujo padre suele
 * terminar en rollback) se persiste igual.
 *
 * <p>Se usa @EventListener (no @TransactionalEventListener) porque LoginFailedEvent NO verá un
 * commit — sale durante un rollback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginAuditListener {

  private static final String ENTIDAD_AUTH = "AUTHENTICATION";

  private final RecordAuditUseCase recordAudit;
  private final AuditJsonSerializer json;

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @EventListener
  public void onLoginSuccess(LoginSuccessEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .empresaId(event.empresaId())
            .sucursalId(event.sucursalId())
            .usuarioId(event.usuarioId())
            .nombreUsuario(event.nombreUsuario())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_AUTH)
            .entidadId(event.usuarioId().toString())
            .accion(AccionAuditoria.LOGIN_SUCCESS)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug("LOGIN_SUCCESS auditado: {}", event.nombreUsuario());
  }

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @EventListener
  public void onLoginFailed(LoginFailedEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .nombreUsuario(event.nombreUsuarioIntento())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_AUTH)
            .entidadId(
                event.nombreUsuarioIntento() != null ? event.nombreUsuarioIntento() : "anonymous")
            .accion(AccionAuditoria.LOGIN_FAILED)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug(
        "LOGIN_FAILED auditado: usuario={} motivo={}",
        event.nombreUsuarioIntento(),
        event.motivo());
  }
}
