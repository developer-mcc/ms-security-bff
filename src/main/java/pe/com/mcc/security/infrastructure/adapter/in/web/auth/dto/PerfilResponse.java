package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record PerfilResponse(
    UUID usuarioId,
    @Nullable UUID empresaId,
    String nombreUsuario,
    String correo,
    String nombres,
    String apellidos,
    @Nullable String dni,
    @Nullable String telefono,
    String canalOtpPreferido,
    boolean mfaHabilitado,
    @Nullable String rol,
    @Nullable UUID sucursalActiva,
    @Nullable String sucursalActivaNombre,
    List<SucursalInfo> sucursalesDisponibles) {}
