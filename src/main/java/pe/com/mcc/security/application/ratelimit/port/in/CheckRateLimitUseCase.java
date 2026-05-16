package pe.com.mcc.security.application.ratelimit.port.in;

import pe.com.mcc.security.domain.ratelimit.model.RateLimitDecision;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitPolicy;

public interface CheckRateLimitUseCase {
  RateLimitDecision check(RateLimitPolicy policy, String key);
}
