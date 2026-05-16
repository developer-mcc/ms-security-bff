package pe.com.mcc.security.application.audit.port.out;

import pe.com.mcc.security.domain.audit.model.AuditEntry;

public interface AuditRepository {
  void save(AuditEntry entry);
}
