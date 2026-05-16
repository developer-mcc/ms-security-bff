package pe.com.mcc.security.infrastructure.adapter.in.web.users.dto;

import java.util.List;
import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    UUID empresaId,
    String nombreUsuario,
    String correo,
    String nombres,
    String apellidos,
    String estado,
    boolean mfaHabilitado,
    List<String> roles) {}
