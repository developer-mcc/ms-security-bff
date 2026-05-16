package pe.com.mcc.security.infrastructure.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.com.mcc.security.domain.ratelimit.model.KeyedBy;

/**
 * Propiedades del rate-limiter bajo el prefijo security.ratelimit.*
 *
 * <p>Ejemplo (application.yaml): security: ratelimit: enabled: true whitelist-ips: [127.0.0.1, ::1]
 * policies: login: path-pattern: /auth/login capacity: 5 refill-period: PT1M keyed-by: IP
 * otp-verify: path-pattern: /auth/otp/verify capacity: 3 refill-period: PT5M keyed-by: USER_ID
 */
@ConfigurationProperties(prefix = "security.ratelimit")
public record RateLimitProperties(
    boolean enabled, List<String> whitelistIps, Map<String, PolicyConfig> policies) {
  public record PolicyConfig(
      String pathPattern, int capacity, Duration refillPeriod, KeyedBy keyedBy) {}
}
