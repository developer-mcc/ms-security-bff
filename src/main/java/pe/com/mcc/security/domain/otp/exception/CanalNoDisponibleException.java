package pe.com.mcc.security.domain.otp.exception;

import pe.com.mcc.security.domain.user.model.CanalOtp;

public class CanalNoDisponibleException extends RuntimeException {

  public CanalNoDisponibleException(CanalOtp canal) {
    super(mensaje(canal));
  }

  private static String mensaje(CanalOtp canal) {
    return switch (canal) {
      case SMS -> "No tienes un número de teléfono registrado. Usa EMAIL como canal alternativo.";
      case WHATSAPP ->
          "No tienes un número de teléfono registrado para WhatsApp. Usa EMAIL como canal"
              + " alternativo.";
      case EMAIL -> "El canal EMAIL siempre está disponible.";
    };
  }
}
