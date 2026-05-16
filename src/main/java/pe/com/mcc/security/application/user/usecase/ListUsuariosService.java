package pe.com.mcc.security.application.user.usecase;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcc.security.application.user.port.in.ListUsuariosUseCase;
import pe.com.mcc.security.application.user.port.out.UserRepository;
import pe.com.mcc.security.domain.user.model.Usuario;

@Service
@RequiredArgsConstructor
public class ListUsuariosService implements ListUsuariosUseCase {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Usuario> listarPorEmpresa(UUID empresaId) {
    return userRepository.findByEmpresaId(empresaId);
  }
}
