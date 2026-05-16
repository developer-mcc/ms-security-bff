package pe.com.mcc.security.infrastructure.adapter.out.event;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.com.mcc.security.application.shared.port.out.IdGenerator;

@Component
public class UuidGeneratorAdapter implements IdGenerator {

  @Override
  public UUID newId() {
    return UUID.randomUUID();
  }
}
