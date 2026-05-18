package pe.com.mcc.security.application.auth.strategy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.domain.user.model.CanalOtp;

@Component
@RequiredArgsConstructor
public class ContactoLookupStrategyFactory {

  private final List<ContactoLookupStrategy> strategies;

  public ContactoLookupStrategy obtener(CanalOtp canal) {
    return strategies.stream()
        .filter(s -> s.soporta(canal))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Canal no soportado: " + canal));
  }
}
