package pe.com.mcc.security.application.auth.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetCommand;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetResult;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetResult.CanalInfo;
import pe.com.mcc.security.application.auth.port.in.SolicitarResetUseCase;
import pe.com.mcc.security.application.auth.port.out.ResetSesionRepository;
import pe.com.mcc.security.application.auth.strategy.ContactoLookupStrategyFactory;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpCommand;
import pe.com.mcc.security.application.otp.port.in.SolicitarOtpUseCase;
import pe.com.mcc.security.domain.auth.exception.ContactoNoRegistradoException;
import pe.com.mcc.security.domain.auth.model.ResetSesion;
import pe.com.mcc.security.domain.otp.model.PropositoOtp;
import pe.com.mcc.security.domain.user.model.Usuario;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitarResetService implements SolicitarResetUseCase {

  private static final int MINUTOS_EXPIRACION_SESION = 15;

  private final ContactoLookupStrategyFactory strategyFactory;
  private final ResetSesionRepository resetSesionRepository;
  private final SolicitarOtpUseCase solicitarOtp;
  private final pe.com.mcc.security.application.shared.port.out.Clock clock;

  @Override
  @Transactional
  public SolicitarResetResult solicitar(SolicitarResetCommand comando) {
    Optional<Usuario> opt = strategyFactory.obtener(comando.canal()).buscar(comando.contacto());
    if (opt.isEmpty()) {
      throw new ContactoNoRegistradoException();
    }
    Usuario usuario = opt.get();

    resetSesionRepository.eliminarPendientes(usuario.id());

    ResetSesion sesion =
        ResetSesion.nueva(usuario.id(), clock.now().plusMinutes(MINUTOS_EXPIRACION_SESION));
    resetSesionRepository.guardar(sesion);

    solicitarOtp.solicitar(
        new SolicitarOtpCommand(
            usuario.id(), comando.canal(), PropositoOtp.RESET_PASSWORD, comando.direccionIp()));

    log.debug("Reset solicitado usuario={} canal={}", usuario.id(), comando.canal());
    return new SolicitarResetResult(sesion.id(), buildCanales(usuario));
  }

  private static List<CanalInfo> buildCanales(Usuario usuario) {
    List<CanalInfo> canales = new ArrayList<>();
    canales.add(new CanalInfo("EMAIL", maskEmail(usuario.correo())));
    if (usuario.telefono() != null && !usuario.telefono().isBlank()) {
      canales.add(new CanalInfo("SMS", maskTelefono(usuario.telefono())));
      canales.add(new CanalInfo("WHATSAPP", maskTelefono(usuario.telefono())));
    }
    return List.copyOf(canales);
  }

  private static String maskEmail(String correo) {
    int at = correo.indexOf('@');
    if (at <= 1) {
      return "***" + correo.substring(at);
    }
    return correo.charAt(0) + "***" + correo.substring(at);
  }

  private static String maskTelefono(String telefono) {
    if (telefono.length() <= 4) {
      return "****";
    }
    return telefono.substring(0, 2) + "***" + telefono.substring(telefono.length() - 3);
  }
}
