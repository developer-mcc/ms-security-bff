package pe.com.mcc.security.domain.auth.exception;

public class ContactoNoRegistradoException extends RuntimeException {

  public ContactoNoRegistradoException() {
    super("No se encontró una cuenta asociada al contacto proporcionado.");
  }
}
