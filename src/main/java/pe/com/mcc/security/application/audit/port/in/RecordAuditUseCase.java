package pe.com.mcc.security.application.audit.port.in;

import pe.com.mcc.security.domain.audit.model.AuditEntry;

public interface RecordAuditUseCase {
  void registrar(AuditEntry entry);
}
