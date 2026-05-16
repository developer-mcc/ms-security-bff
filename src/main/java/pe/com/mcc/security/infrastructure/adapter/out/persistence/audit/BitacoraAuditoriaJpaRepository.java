package pe.com.mcc.security.infrastructure.adapter.out.persistence.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitacoraAuditoriaJpaRepository
    extends JpaRepository<BitacoraAuditoriaJpaEntity, UUID> {}
