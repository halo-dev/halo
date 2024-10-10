package run.halo.app.core.user.service;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import run.halo.app.infra.ValidationUtils;

/**
 * Sign up data.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Data
@SignUpData.SignUpDataConstraint
public class SignUpData {

    @NotBlank
    @Size(min = 4, max = 63)
    @Pattern(regexp = ValidationUtils.NAME_REGEX,
        message = "{validation.error.username.pattern}")
    private String username;

    @NotBlank
    private String displayName;

    @Email
    private String email;

    private String emailCode;

    @NotBlank
    @Size(min = 5, max = 257)
    @Pattern(regexp = ValidationUtils.PASSWORD_REGEX,
        message = "{validation.error.password.pattern}")
    private String password;

    @NotBlank
    private String confirmPassword;

    public static SignUpData of(MultiValueMap<String, String> formData) {
        var form = new SignUpData();
        Optional.ofNullable(formData.getFirst("username"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setUsername);

        Optional.ofNullable(formData.getFirst("displayName"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setDisplayName);

        Optional.ofNullable(formData.getFirst("email"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setEmail);

        Optional.ofNullable(formData.getFirst("password"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setPassword);

        Optional.ofNullable(formData.getFirst("emailCode"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setEmailCode);

        Optional.ofNullable(formData.getFirst("confirmPassword"))
            .filter(StringUtils::hasText)
            .ifPresent(form::setConfirmPassword);

        return form;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {SignUpDataConstraintValidator.class})
    public @interface SignUpDataConstraint {

        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    private static class SignUpDataConstraintValidator
        implements ConstraintValidator<SignUpDataConstraint, SignUpData> {

        @Override
        public boolean isValid(SignUpData signUpData, ConstraintValidatorContext context) {
            var isValid = Objects.equals(signUpData.getPassword(), signUpData.getConfirmPassword());
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "{signup.error.confirm-password-not-match}"
                    )
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
            }
            return isValid;
        }
    }
}
