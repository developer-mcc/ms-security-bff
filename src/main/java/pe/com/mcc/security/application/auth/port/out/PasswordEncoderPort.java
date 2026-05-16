package pe.com.mcc.security.application.auth.port.out;

/**
 * Abstrae el algoritmo de hash. La impl. por defecto es BCrypt (BcryptPasswordEncoderAdapter), pero
 * el caso de uso no lo sabe.
 */
public interface PasswordEncoderPort {
  String encode(CharSequence rawPassword);

  boolean matches(CharSequence rawPassword, String encodedPassword);
}
