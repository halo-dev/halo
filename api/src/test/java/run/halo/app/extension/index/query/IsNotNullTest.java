package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IsNotNull}.
 *
 * @author guqing
 * @since 2.17.0
 */
class IsNotNullTest {

    @Test
    void testToString() {
        var isNotNull = new IsNotNull("name");
        assertThat(isNotNull.toString()).isEqualTo("name IS NOT NULL");
    }
}