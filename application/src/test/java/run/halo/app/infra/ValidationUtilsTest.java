package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValidationUtils}.
 *
 * @author guqing
 * @since 2.5.0
 */
class ValidationUtilsTest {

    @Nested
    class NameValidationTest {
        @Test
        void nullName() {
            assertThat(ValidationUtils.validateName(null)).isFalse();
        }

        @Test
        void emptyUsername() {
            assertThat(ValidationUtils.validateName("")).isFalse();
        }

        @Test
        void startWithIllegalCharacter() {
            assertThat(ValidationUtils.validateName("-abc")).isFalse();
        }

        @Test
        void endWithIllegalCharacter() {
            assertThat(ValidationUtils.validateName("abc-")).isFalse();
            assertThat(ValidationUtils.validateName("abcD")).isFalse();
        }

        @Test
        void middleWithIllegalCharacter() {
            assertThat(ValidationUtils.validateName("ab?c")).isFalse();
        }

        @Test
        void moreThan63Characters() {
            assertThat(ValidationUtils.validateName(StringUtils.repeat('a', 64))).isFalse();
        }

        @Test
        void correctUsername() {
            assertThat(ValidationUtils.validateName("abc")).isTrue();
            assertThat(ValidationUtils.validateName("ab-c")).isTrue();
            assertThat(ValidationUtils.validateName("1st")).isTrue();
            assertThat(ValidationUtils.validateName("ast1")).isTrue();
            assertThat(ValidationUtils.validateName("ast-1")).isTrue();
        }
    }

    @Test
    void validateEmailTest() {
        var cases = new HashMap<String, Boolean>();
        // Valid cases
        cases.put("simple@example.com", true);
        cases.put("very.common@example.com", true);
        cases.put("disposable.style.email.with+symbol@example.com", true);
        cases.put("other.email-with-hyphen@example.com", true);
        cases.put("fully-qualified-domain@example.com", true);
        cases.put("user.name+tag+sorting@example.com", true);
        cases.put("x@example.com", true);
        cases.put("example-indeed@strange-example.com", true);
        cases.put("example@s.example", true);
        cases.put("john.doe@example.com", true);
        cases.put("a.little.lengthy.but.fine@dept.example.com", true);
        cases.put("123ada@halo.co", true);
        cases.put("23ad@halo.top", true);

        // Invalid cases
        cases.put("Abc.example.com", false);
        cases.put("admin@mailserver1", false);
        cases.put("\" \"@example.org", false);
        cases.put("A@b@c@example.com", false);
        cases.put("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com", false);
        cases.put("just\"not\"right@example.com", false);
        cases.put("this is\"not\\allowed@example.com", false);
        cases.put("this\\ still\\\"not\\\\allowed@example.com", false);
        cases.put("123456789012345678901234567890123456789012345", false);
        cases.forEach((email, expected) -> assertThat(ValidationUtils.isValidEmail(email))
            .isEqualTo(expected));
    }
}