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
}