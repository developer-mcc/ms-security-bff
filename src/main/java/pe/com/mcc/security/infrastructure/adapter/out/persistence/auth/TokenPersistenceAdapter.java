package pe.com.mcc.security.infrastructure.adapter.out.persistence.auth;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.shared.port.out.Clock;
import pe.com.mcc.security.application.token.port.out.TokenRepository;
import pe.com.mcc.security.domain.token.model.MotivoRevocacion;
import pe.com.mcc.security.domain.token.model.Token;

@Component
@RequiredArgsConstructor
public class TokenPersistenceAdapter implements TokenRepository {

  private final TokenJpaRepository tokenJpaRepository;
  private final TokenMapper tokenMapper;
  private final Clock clock;

  @Override
  public void save(Token token) {
    tokenJpaRepository.save(tokenMapper.toEntity(token));
  }

  @Override
  public Optional<Token> findByJti(UUID jti) {
    return tokenJpaRepository.findById(jti).map(tokenMapper::toDomain);
  }

  @Override
  public int revokeBySesionId(UUID sesionId, MotivoRevocacion motivo) {
    return tokenJpaRepository.revokeBySesionId(sesionId, motivo.name(), clock.now());
  }

  @Override
  public int revokeByUsuarioId(UUID usuarioId, MotivoRevocacion motivo) {
    return tokenJpaRepository.revokeByUsuarioId(usuarioId, motivo.name(), clock.now());
  }
}
