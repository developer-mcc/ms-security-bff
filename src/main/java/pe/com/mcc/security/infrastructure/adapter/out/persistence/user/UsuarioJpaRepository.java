package pe.com.mcc.security.infrastructure.adapter.out.persistence.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

  Optional<UsuarioJpaEntity> findByNombreUsuario(String nombreUsuario);

  List<UsuarioJpaEntity> findByEmpresaId(UUID empresaId);
}
