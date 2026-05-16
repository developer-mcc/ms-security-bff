package pe.com.mcc.security.domain.user.model;

import java.util.UUID;

/**
 * Sucursal habilitada para un usuario, con marca de predeterminada. Usado por AuthenticateService
 * para resolver la sucursal activa post-login.
 */
public record SucursalUsuario(UUID sucursalId, String nombre, boolean esPredeterminada) {}
