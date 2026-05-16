package pe.com.mcc.security.domain.permission.model;

/**
 * Recurso protegido. WILDCARD ('*') concede acceso a cualquier recurso (SUPER_ADMIN). Los nombres
 * se mantienen en inglés porque viajan en el JWT y aparecen en @PreAuthorize / hasPermission(...).
 */
public final class Recurso {
  public static final String WILDCARD = "*";
  public static final String PRODUCT = "PRODUCT";
  public static final String STOCK = "STOCK";
  public static final String SALE = "SALE";
  public static final String CLIENT = "CLIENT";
  public static final String USER = "USER";
  public static final String BRANCH = "BRANCH";
  public static final String REPORT = "REPORT";
  public static final String SUPPLIER = "SUPPLIER";
  public static final String PURCHASE_ORDER = "PURCHASE_ORDER";
  public static final String PRESCRIPTION = "PRESCRIPTION";
  public static final String AUDIT = "AUDIT";

  private Recurso() {}
}
