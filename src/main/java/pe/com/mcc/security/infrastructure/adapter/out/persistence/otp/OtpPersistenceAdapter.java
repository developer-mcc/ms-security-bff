package pe.com.mcc.security.infrastructure.adapter.out.persistence.otp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.otp.port.out.OtpRepository;
import pe.com.mcc.security.domain.otp.model.CodigoOtp;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;

@Component
@RequiredArgsConstructor
public class OtpPersistenceAdapter implements OtpRepository {

  private final OtpJpaRepository jpaRepository;
  private final CodigoOtpMapper mapper;

  @Override
  public CodigoOtp guardar(CodigoOtp otp) {
    CodigoOtpJpaEntity saved = jpaRepository.save(mapper.toEntity(otp));
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<CodigoOtp> buscarParaVerificar(UUID usuarioId, PropositoOtp proposito) {
    return jpaRepository
        .findParaVerificar(usuarioId, proposito.name(), LocalDateTime.now())
        .map(mapper::toDomain);
  }

  @Override
  public void invalidarPendientes(UUID usuarioId, PropositoOtp proposito) {
    jpaRepository.invalidarPendientes(usuarioId, proposito.name(), LocalDateTime.now());
  }

  @Override
  public int eliminarExpiradosYUsados() {
    return jpaRepository.eliminarExpiradosYUsados(LocalDateTime.now());
  }
}
