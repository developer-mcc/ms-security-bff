package pe.com.mcc.security.infrastructure.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ResetPasswordAnonimRequest(
    @NotNull UUID resetToken,
    @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message =
                "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un"
                    + " número.")
        String nuevaContrasena) {}
