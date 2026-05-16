package pe.com.mcc.security.infrastructure.adapter.in.web.users;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.mcc.security.application.user.port.in.CrearUsuarioUseCase;
import pe.com.mcc.security.application.user.port.in.ListUsuariosUseCase;
import pe.com.mcc.security.infrastructure.adapter.in.web.users.dto.CrearUsuarioRequest;
import pe.com.mcc.security.infrastructure.adapter.in.web.users.dto.UsuarioResponse;
import pe.com.mcc.security.infrastructure.adapter.in.web.users.mapper.UsuarioDtoMapper;
import pe.com.mcc.security.infrastructure.adapter.out.security.jwt.UserPrincipal;

/**
 * Endpoints de gestión de usuarios. Demuestran el modelo 3D vía @PreAuthorize.
 *
 * <p>hasPermission(target, recurso, accion) - target = null → el evaluator considera OWN_BRANCH
 * (mínimo). - recurso 'USER' debe estar en el PermissionMap del JWT. - accion debe estar en el
 * array de acciones del permiso.
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

  private final ListUsuariosUseCase listUsuarios;
  private final CrearUsuarioUseCase crearUsuario;
  private final UsuarioDtoMapper mapper;

  @GetMapping
  @PreAuthorize("hasPermission(null, 'USER', 'READ')")
  public ResponseEntity<List<UsuarioResponse>> listar(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) UUID empresaId) {
    UUID target = empresaId != null ? empresaId : principal.empresaId();
    List<UsuarioResponse> body =
        listUsuarios.listarPorEmpresa(target).stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @PostMapping
  @PreAuthorize("hasPermission(null, 'USER', 'CREATE')")
  public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest body) {
    var nuevo = crearUsuario.crear(mapper.toCommand(body));
    return ResponseEntity.ok(mapper.toResponse(nuevo));
  }
}
