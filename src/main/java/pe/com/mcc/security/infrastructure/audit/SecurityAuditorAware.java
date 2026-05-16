package pe.com.mcc.security.infrastructure.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

@Component("securityAuditorAware")
public class SecurityAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
      return Optional.of("SYSTEM");
    }
    if (auth.getPrincipal() instanceof UserPrincipal up) {
      return Optional.of(up.nombreUsuario());
    }
    return Optional.of(auth.getName());
  }
}
