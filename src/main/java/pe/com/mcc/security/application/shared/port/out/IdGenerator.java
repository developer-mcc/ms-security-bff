package pe.com.mcc.security.application.shared.port.out;

import java.util.UUID;

public interface IdGenerator {
  UUID newId();
}
