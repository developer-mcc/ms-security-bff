package pe.com.mcc.security.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Encapsula la serialización a JSON de los payloads que los listeners almacenan en
 * bitacora_auditoria.valor_nuevo. Aislar Jackson aquí evita que cada listener lo importe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditJsonSerializer {

  private final ObjectMapper objectMapper;

  public String toJson(Object payload) {
    if (payload == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JacksonException e) {
      log.warn("No se pudo serializar payload de auditoría", e);
      return "{\"error\":\"serialization-failed\"}";
    }
  }
}
