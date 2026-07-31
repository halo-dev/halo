package run.halo.app.core.user.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import run.halo.app.infra.ValidationUtils;

/**
 * Registration data for OAuth2 users who choose to create a new account and bind it to the provider.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Data
public class OAuth2RegisterData {

    @NotBlank
    @Size(min = 4, max = 63)
    @Pattern(regexp = ValidationUtils.NAME_REGEX, message = "{validation.error.username.pattern}")
    private String username;

    @NotBlank
    private String displayName;

    @Email
    private String email;

    private Boolean agreedToTerms;
}
