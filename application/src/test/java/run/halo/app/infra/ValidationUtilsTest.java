package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(validateName(null)).isFalse();
        }

        @Test
        void emptyUsername() {
            assertThat(validateName("")).isFalse();
        }

        @Test
        void startWithIllegalCharacter() {
            assertThat(validateName("-abc")).isFalse();
        }

        @Test
        void endWithIllegalCharacter() {
            assertThat(validateName("abc-")).isFalse();
            assertThat(validateName("abcD")).isFalse();
        }

        @Test
        void middleWithIllegalCharacter() {
            assertThat(validateName("ab?c")).isFalse();
        }

        @Test
        void moreThan63Characters() {
            assertThat(validateName(StringUtils.repeat('a', 64))).isFalse();
        }

        @Test
        void correctUsername() {
            assertThat(validateName("abc")).isTrue();
            assertThat(validateName("ab-c")).isTrue();
            assertThat(validateName("1st")).isTrue();
            assertThat(validateName("ast1")).isTrue();
            assertThat(validateName("ast-1")).isTrue();
        }

        static boolean validateName(String name) {
            if (StringUtils.isBlank(name)) {
                return false;
            }
            boolean matches = ValidationUtils.NAME_PATTERN.matcher(name).matches();
            return matches && name.length() <= 63;
        }
    }
}
