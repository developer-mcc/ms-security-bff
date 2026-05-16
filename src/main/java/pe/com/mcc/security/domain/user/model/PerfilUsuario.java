package pe.com.mcc.security.domain.user.model;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Proyección de dominio para el perfil visible del usuario. Excluye campos sensibles
 * (contrasenaHash, intentosFallidos, bloqueadoHasta) que no deben exponerse fuera del flujo de
 * autenticación.
 */
@NullMarked
public record PerfilUsuario(
    UUID id,
    @Nullable UUID empresaId,
    String nombreUsuario,
    String correo,
    String nombres,
    String apellidos,
    @Nullable String dni,
    @Nullable String telefono,
    CanalOtp canalOtpPreferido,
    boolean mfaHabilitado,
    @Nullable String rol,
    @Nullable UUID sucursalActiva,
    @Nullable String sucursalActivaNombre,
    List<SucursalUsuario> sucursalesDisponibles) {}
