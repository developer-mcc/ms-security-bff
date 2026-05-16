package pe.com.mcc.security.infrastructure.adapter.out.persistence.permission;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermisoRolRecursoJpaRepository
    extends JpaRepository<PermisoRolRecursoJpaEntity, UUID> {

  @Query(
      """
      SELECT p FROM PermisoRolRecursoJpaEntity p
        JOIN UsuarioRolJpaEntity ur ON ur.rol.id = p.rol.id
       WHERE ur.usuario.id = :usuarioId
      """)
  List<PermisoRolRecursoJpaEntity> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
