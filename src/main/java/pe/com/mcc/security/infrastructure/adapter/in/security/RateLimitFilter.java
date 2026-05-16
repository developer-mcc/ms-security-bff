package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.com.mcc.security.application.ratelimit.port.in.CheckRateLimitUseCase;
import pe.com.mcc.security.domain.ratelimit.model.KeyedBy;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitDecision;
import pe.com.mcc.security.domain.ratelimit.model.RateLimitPolicy;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;
import pe.com.mcc.security.infrastructure.config.RateLimitProperties;

/**
 * Aplica políticas de rate-limit declaradas en application.yaml a paths específicos.
 *
 * <p>SEQ_10: si security.ratelimit.enabled=false, el filter es no-op (pasa todo). SEQ_11: IPs en
 * whitelist saltan el rate-limit (útil para health-checks internos, CI). SEQ_12: busca la primera
 * policy cuyo path-pattern matchea el request URI. Si ninguna coincide, no aplica rate-limit (otros
 * endpoints quedan ilimitados). SEQ_13: para keyedBy=USER_ID, lee SecurityContext. Como este filter
 * va DESPUÉS del JwtAuthenticationFilter, el principal ya está poblado si la petición lleva JWT
 * válido. Si no hay principal, fallback a IP — más conservador. SEQ_14: cuando se rechaza, escribe
 * ProblemDetail con header Retry-After y headers informativos (X-RateLimit-Remaining=0,
 * X-RateLimit-Limit=capacity).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private final RateLimitProperties properties;
  private final CheckRateLimitUseCase checkRateLimit;
  private final ProblemDetailWriter writer;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    // SEQ_10
    if (!properties.enabled() || properties.policies() == null) {
      chain.doFilter(request, response);
      return;
    }

    String ip = resolveIp(request);

    // SEQ_11
    if (properties.whitelistIps() != null && properties.whitelistIps().contains(ip)) {
      chain.doFilter(request, response);
      return;
    }

    // SEQ_12
    Map.Entry<String, RateLimitProperties.PolicyConfig> match =
        matchPolicy(request.getRequestURI());
    if (match == null) {
      chain.doFilter(request, response);
      return;
    }

    RateLimitProperties.PolicyConfig cfg = match.getValue();
    RateLimitPolicy policy =
        new RateLimitPolicy(match.getKey(), cfg.capacity(), cfg.refillPeriod(), cfg.keyedBy());

    // SEQ_13
    String key = resolveKey(cfg.keyedBy(), ip);

    RateLimitDecision decision = checkRateLimit.check(policy, key);

    if (!decision.allowed()) {
      // SEQ_14
      log.warn(
          "Rate-limit excedido policy={} key={} retryAfter={}s",
          policy.name(),
          key,
          decision.retryAfterSeconds());
      response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(decision.retryAfterSeconds()));
      response.setHeader("X-RateLimit-Limit", String.valueOf(policy.capacity()));
      response.setHeader("X-RateLimit-Remaining", "0");
      writer.write(
          response,
          HttpStatus.TOO_MANY_REQUESTS,
          "rate-limit-exceeded",
          "Demasiadas peticiones",
          "Has superado el límite de peticiones para esta operación. Reintenta en "
              + decision.retryAfterSeconds()
              + " segundos.");
      return;
    }

    response.setHeader("X-RateLimit-Limit", String.valueOf(policy.capacity()));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
    chain.doFilter(request, response);
  }

  private Map.Entry<String, RateLimitProperties.PolicyConfig> matchPolicy(String uri) {
    for (Map.Entry<String, RateLimitProperties.PolicyConfig> e : properties.policies().entrySet()) {
      if (PATH_MATCHER.match(e.getValue().pathPattern(), uri)) {
        return e;
      }
    }
    return null;
  }

  private String resolveKey(KeyedBy keyedBy, String ip) {
    if (keyedBy == KeyedBy.USER_ID) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
        return "user:" + up.usuarioId();
      }
      // fallback: petición no autenticada llegando a un endpoint USER_ID-keyed → IP
      return "ip:" + ip;
    }
    return "ip:" + ip;
  }

  private static String resolveIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
    }
    return request.getRemoteAddr();
  }
}
