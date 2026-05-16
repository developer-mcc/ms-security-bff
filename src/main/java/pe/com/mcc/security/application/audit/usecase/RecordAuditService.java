package pe.com.mcc.security.application.audit.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.audit.port.in.RecordAuditUseCase;
import pe.com.mcc.security.application.audit.port.out.AuditRepository;
import pe.com.mcc.security.domain.audit.model.AuditEntry;

/**
 * REQUIRES_NEW: el listener de LoginFailedEvent corre dentro del rollback del flujo de login, pero
 * la entrada de bitácora debe persistirse igual. Una transacción independiente garantiza que el
 * rollback del padre no la arrastre.
 */
@Service
@RequiredArgsConstructor
public class RecordAuditService implements RecordAuditUseCase {

  private final AuditRepository auditRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrar(AuditEntry entry) {
    auditRepository.save(entry);
  }
}
