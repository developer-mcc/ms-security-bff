package pe.com.mcc.security.application.ratelimit.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.mcc.security.application.ratelimit.port.in.CheckRateLimitUseCase;
import pe.com.mcc.security.application.ratelimit.port.out.RateLimitStore;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitDecision;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitPolicy;

/**
 * Servicio thin: delega al RateLimitStore. Existe como capa explícita para que el filtro NO dependa
 * directamente del puerto out (DIP) y para poder agregar orchestration extra en el futuro
 * (métricas, eventos, etc.) sin tocar el filter.
 */
@Service
@RequiredArgsConstructor
public class CheckRateLimitService implements CheckRateLimitUseCase {

  private final RateLimitStore store;

  @Override
  public RateLimitDecision check(RateLimitPolicy policy, String key) {
    return store.tryConsume(policy, key);
  }
}
