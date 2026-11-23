package run.halo.app.core.user.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.ValidationUtils;

class SignUpDataTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectDisplayNameExceedingMaxLength() {
        var signUpData = validSignUpData();
        signUpData.setDisplayName("a".repeat(ValidationUtils.DISPLAY_NAME_MAX_LENGTH + 1));

        var violations = validator.validateProperty(signUpData, "displayName");

        assertTrue(violations.stream()
                .anyMatch(violation ->
                        "displayName".equals(violation.getPropertyPath().toString())));
    }

    @Test
    void shouldAcceptDisplayNameAtMaxLength() {
        var signUpData = validSignUpData();
        signUpData.setDisplayName("a".repeat(ValidationUtils.DISPLAY_NAME_MAX_LENGTH));

        assertTrue(validator.validateProperty(signUpData, "displayName").isEmpty());
    }

    private SignUpData validSignUpData() {
        var signUpData = new SignUpData();
        signUpData.setUsername("fake-user");
        signUpData.setDisplayName("Fake User");
        signUpData.setPassword("fake-password");
        signUpData.setConfirmPassword("fake-password");
        return signUpData;
    }
}
