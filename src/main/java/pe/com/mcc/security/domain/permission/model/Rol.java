package pe.com.mcc.security.domain.permission.model;

public record Rol(String id, String nombre, String descripcion) {

  public static final String SUPER_ADMIN = "SUPER_ADMIN";
  public static final String ADMIN_EMPRESA = "ADMIN_EMPRESA";
  public static final String ADMIN_SUCURSAL = "ADMIN_SUCURSAL";
  public static final String FARMACEUTICO = "FARMACEUTICO";
  public static final String CAJERO = "CAJERO";
  public static final String AUDITOR = "AUDITOR";
}
