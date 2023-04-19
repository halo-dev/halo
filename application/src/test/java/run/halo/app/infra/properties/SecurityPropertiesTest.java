package run.halo.app.infra.properties;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SecurityProperties}.
 *
 * @author guqing
 * @since 2.5.0
 */
class SecurityPropertiesTest {

    @Nested
    class InitializerTest {
        private Validator validator;

        @BeforeEach
        void setUp() {
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                validator = factory.getValidator();
            }
        }

        @Test
        void nullUsername() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername(null);
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Super administrator username must not be blank");
        }

        @Test
        void emptyUsername() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername("");
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(2);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Super administrator username must not be blank");
        }

        @Test
        void startWithIllegalCharacter() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername("-abc");
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo(expectedErrorMessage());
        }

        @Test
        void endWithIllegalCharacter() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername("abc-");
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo(expectedErrorMessage());

            initializer.setSuperAdminUsername("abcD");
            violations = validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo(expectedErrorMessage());

        }

        @Test
        void middleWithIllegalCharacter() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername("ab?c");
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo(expectedErrorMessage());
        }

        @Test
        void moreThan63Characters() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername(StringUtils.repeat('a', 64));
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Must be less than 63 characters");
        }

        @Test
        void correctUsername() {
            SecurityProperties.Initializer initializer = new SecurityProperties.Initializer();
            initializer.setSuperAdminUsername("abc");
            Set<ConstraintViolation<SecurityProperties.Initializer>> violations =
                validator.validate(initializer);
            assertThat(violations).isEmpty();

            initializer.setSuperAdminUsername("ab-c");
            violations = validator.validate(initializer);
            assertThat(violations).isEmpty();

            initializer.setSuperAdminUsername("1st");
            violations = validator.validate(initializer);
            assertThat(violations).isEmpty();

            initializer.setSuperAdminUsername("ast1");
            violations = validator.validate(initializer);
            assertThat(violations).isEmpty();

            initializer.setSuperAdminUsername("ast-1");
            violations = validator.validate(initializer);
            assertThat(violations).isEmpty();
        }

        private String expectedErrorMessage() {
            return """
                Super administrator username must be a valid subdomain name, the name must:
                1. contain no more than 63 characters
                2. contain only lowercase alphanumeric characters, '-' or '.'
                3. start with an alphanumeric character
                4. end with an alphanumeric character
                """;
        }
    }
}