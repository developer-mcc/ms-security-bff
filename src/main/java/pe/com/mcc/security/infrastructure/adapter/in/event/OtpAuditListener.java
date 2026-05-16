package pe.com.mcc.security.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.audit.port.in.RecordAuditUseCase;
import pe.com.mcc.security.domain.audit.model.AccionAuditoria;
import pe.com.mcc.security.domain.audit.model.AuditEntry;
import pe.com.mcc.security.domain.otp.event.OtpFallidoEvent;
import pe.com.mcc.security.domain.otp.event.OtpSolicitadoEvent;
import pe.com.mcc.security.domain.otp.event.OtpVerificadoEvent;
import pe.com.mcc.security.infrastructure.config.AsyncConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpAuditListener {

  private static final String ENTIDAD_OTP = "OTP";

  private final RecordAuditUseCase recordAudit;
  private final AuditJsonSerializer json;

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @EventListener
  public void onOtpSolicitado(OtpSolicitadoEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .usuarioId(event.usuarioId())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_OTP)
            .entidadId(event.usuarioId().toString())
            .accion(AccionAuditoria.OTP_REQUESTED)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug("OTP_REQUESTED auditado: usuario={}", event.usuarioId());
  }

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @EventListener
  public void onOtpVerificado(OtpVerificadoEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .usuarioId(event.usuarioId())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_OTP)
            .entidadId(event.usuarioId().toString())
            .accion(AccionAuditoria.OTP_VERIFIED)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug("OTP_VERIFIED auditado: usuario={}", event.usuarioId());
  }

  @Async(AsyncConfig.SECURITY_EVENTS_EXECUTOR)
  @EventListener
  public void onOtpFallido(OtpFallidoEvent event) {
    AuditEntry entry =
        AuditEntry.builder()
            .usuarioId(event.usuarioId())
            .direccionIp(event.direccionIp())
            .tipoEntidad(ENTIDAD_OTP)
            .entidadId(event.usuarioId().toString())
            .accion(AccionAuditoria.OTP_FAILED)
            .valorNuevo(json.toJson(event))
            .build();
    recordAudit.registrar(entry);
    log.debug("OTP_FAILED auditado: usuario={} motivo={}", event.usuarioId(), event.motivo());
  }
}
