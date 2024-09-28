package run.halo.app.core.user.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import lombok.Data;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Sign up data.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Data
public class SignUpData {

    @NotBlank
    private String username;

    @NotBlank
    private String displayName;

    @Email
    private String email;

    private String emailCode;

    @NotBlank
    private String password;

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

        return form;
    }
}
