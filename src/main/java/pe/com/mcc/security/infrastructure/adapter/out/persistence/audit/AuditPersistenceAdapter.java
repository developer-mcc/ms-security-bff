package pe.com.mcc.security.infrastructure.adapter.out.persistence.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.audit.port.out.AuditRepository;
import pe.com.mcc.security.domain.audit.model.AuditEntry;

@Component
@RequiredArgsConstructor
public class AuditPersistenceAdapter implements AuditRepository {

  private final BitacoraAuditoriaJpaRepository jpa;

  @Override
  public void save(AuditEntry entry) {
    BitacoraAuditoriaJpaEntity e = new BitacoraAuditoriaJpaEntity();
    e.setEmpresaId(entry.empresaId());
    e.setSucursalId(entry.sucursalId());
    e.setUsuarioId(entry.usuarioId());
    e.setNombreUsuario(entry.nombreUsuario());
    e.setDireccionIp(entry.direccionIp());
    e.setAgenteUsuario(entry.agenteUsuario());
    e.setTipoEntidad(entry.tipoEntidad());
    e.setEntidadId(entry.entidadId());
    e.setAccion(entry.accion().name());
    e.setValorAnterior(entry.valorAnterior());
    e.setValorNuevo(entry.valorNuevo());
    jpa.save(e);
  }
}
