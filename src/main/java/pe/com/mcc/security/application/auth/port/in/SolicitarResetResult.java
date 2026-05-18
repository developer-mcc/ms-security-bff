package pe.com.mcc.security.application.auth.port.in;

import java.util.List;
import java.util.UUID;

public record SolicitarResetResult(UUID preResetToken, List<CanalInfo> canalesDisponibles) {

  public record CanalInfo(String tipo, String valorMascarado) {}
}
