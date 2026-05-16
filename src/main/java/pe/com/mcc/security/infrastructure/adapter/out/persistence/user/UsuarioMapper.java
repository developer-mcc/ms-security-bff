package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.user.model.CanalOtp;
import pe.com.mcc.security.domain.user.model.EstadoUsuario;
import pe.com.mcc.security.domain.user.model.Usuario;

/**
 * Mapper JpaEntity <-> Usuario (modelo dominio puro). Aislado para que el persistence-adapter no
 * haga el mapeo inline.
 */
@Component
public class UsuarioMapper {

  public Usuario toDomain(UsuarioJpaEntity e) {
    List<String> roles = e.getRoles().stream().map(r -> r.getRol().getId()).toList();

    return new Usuario(
        e.getId(),
        e.getEmpresaId(),
        e.getNombreUsuario(),
        e.getCorreo(),
        e.getContrasenaHash(),
        e.getNombres(),
        e.getApellidos(),
        e.getDni(),
        e.getTelefono(),
        Optional.ofNullable(e.getCanalOtpPreferido()).map(CanalOtp::valueOf).orElse(CanalOtp.EMAIL),
        e.isMfaHabilitado(),
        EstadoUsuario.valueOf(e.getEstado()),
        e.getIntentosFallidos(),
        e.getBloqueadoHasta(),
        e.getUltimoAcceso(),
        roles);
  }

  public void copyMutableFieldsToEntity(Usuario d, UsuarioJpaEntity e) {
    e.setEstado(d.estado().name());
    e.setIntentosFallidos(d.intentosFallidos());
    e.setBloqueadoHasta(d.bloqueadoHasta());
    e.setUltimoAcceso(d.ultimoAcceso());
  }
}
