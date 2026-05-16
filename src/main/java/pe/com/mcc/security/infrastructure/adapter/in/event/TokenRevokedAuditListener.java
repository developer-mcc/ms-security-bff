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
import pe.com.mcc.security.domain.token.event.TokenRevokedEvent;
import pe.com.mcc.security.infrastructure.config.AsyncConfig;

/**
 * Audita revocaciones de token (logout, logout-all, branch-switch,
 * refresh-used). @TransactionalEventListener(AFTER_COMMIT): solo registra si la revocación
 * realmente se persistió en sec.tokens. Si la transacción del LogoutService rollback, no generamos
 * una entrada falsa de "logout exitoso".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRevokedAuditListener {

  private static final String ENTIDAD_SESION = "SESSION";

  private final RecordAuditUseCase recordAudit;
  private final AuditJsonSerializer json;

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTokenRevoked(TokenRevokedEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .usuarioId(event.usuarioId())
            .tipoEntidad(ENTIDAD_SESION)
            .entidadId(resolverEntidadId(event))
            .accion(AccionAuditoria.TOKEN_REVOKED)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug(
        "TOKEN_REVOKED auditado: motivo={} tokens={}", event.motivo(), event.tokensRevocados());
  }

  /**
   * Determina la entidad_id para la bitácora:
   *
   * <ul>
   *   <li>logout / branch-switch / refresh-used → sesion_id (entidad SESSION concreta).
   *   <li>logout-all → usuario_id (no hay una sesión específica).
   *   <li>casos defensivos (evento sin ambos) → "n/a".
   * </ul>
   */
  private static String resolverEntidadId(TokenRevokedEvent event) {
    if (event.sesionId() != null) {
      return event.sesionId().toString();
    }
    if (event.usuarioId() != null) {
      return event.usuarioId().toString();
    }
    return "n/a";
  }
}
