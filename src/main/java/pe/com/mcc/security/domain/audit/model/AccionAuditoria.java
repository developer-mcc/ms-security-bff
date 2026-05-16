package pe.com.mcc.security.domain.audit.model;

/**
 * Catálogo cerrado de acciones que se registran en sec.bitacora_auditoria. Coincide con el CHECK
 * constraint de la columna accion.
 */
public enum AccionAuditoria {
  CREATE,
  UPDATE,
  DELETE,
  LOGIN_SUCCESS,
  LOGIN_FAILED,
  LOGOUT,
  OTP_REQUESTED,
  OTP_VERIFIED,
  OTP_FAILED,
  TOKEN_ISSUED,
  TOKEN_REVOKED,
  TOKEN_REFRESHED,
  BRANCH_SWITCHED,
  USER_BLOCKED,
  USER_UNBLOCKED,
  PASSWORD_CHANGED,
  PERMISSION_DENIED
}
