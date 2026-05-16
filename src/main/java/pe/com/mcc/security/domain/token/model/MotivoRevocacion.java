package pe.com.mcc.security.domain.token.model;

public enum MotivoRevocacion {
  LOGOUT,
  LOGOUT_ALL,
  BRANCH_SWITCH,
  REFRESH_USED,
  ADMIN_REVOKE,
  PASSWORD_CHANGED,
  USER_BLOCKED,
  SECURITY_INCIDENT
}
