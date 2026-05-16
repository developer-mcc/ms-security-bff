package pe.com.mcc.security.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.mcc.security.application.audit.port.in.RecordAuditUseCase;
import pe.com.mcc.security.domain.audit.model.AccionAuditoria;
import pe.com.mcc.security.domain.audit.model.AuditEntry;
import pe.com.mcc.security.domain.auth.event.BranchSwitchedEvent;
import pe.com.mcc.security.infrastructure.config.AsyncConfig;

/**
 * Persiste BRANCH_SWITCHED en sec.bitacora_auditoria. AFTER_COMMIT garantiza que solo registra
 * cambios efectivamente persistidos (la sesión vieja revocada y la nueva emitida).
 *
 * <p>tipo_entidad = SESSION; entidad_id = sesionAnterior. valor_anterior y valor_nuevo guardan los
 * IDs de sucursal antes/después para auditoría legible.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BranchSwitchedAuditListener {

  private static final String ENTIDAD_SESION = "SESSION";

  private final RecordAuditUseCase recordAudit;
  private final AuditJsonSerializer json;

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBranchSwitched(BranchSwitchedEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .empresaId(event.empresaId())
            .sucursalId(event.sucursalNueva())
            .usuarioId(event.usuarioId())
            .nombreUsuario(event.nombreUsuario())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_SESION)
            .entidadId(event.sesionAnterior() != null ? event.sesionAnterior().toString() : "n/a")
            .accion(AccionAuditoria.BRANCH_SWITCHED)
            .valorAnterior(
                json.toJson(
                    java.util.Map.of(
                        "sesionId", event.sesionAnterior(),
                        "sucursalId", event.sucursalAnterior())))
            .valorNuevo(
                json.toJson(
                    java.util.Map.of(
                        "sesionId", event.sesionNueva(),
                        "sucursalId", event.sucursalNueva())))
            .build();
    recordAudit.registrar(entry);
    log.debug(
        "BRANCH_SWITCHED auditado: usuario={} {} -> {}",
        event.nombreUsuario(),
        event.sucursalAnterior(),
        event.sucursalNueva());
  }
}
