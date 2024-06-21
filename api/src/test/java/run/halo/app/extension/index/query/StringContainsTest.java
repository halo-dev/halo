package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringContains}.
 *
 * @author guqing
 * @since 2.17.0
 */
class StringContainsTest {

    @Test
    void testToString() {
        var stringContains = new StringContains("name", "Alice");
        assertThat(stringContains.toString()).isEqualTo("contains(name, 'Alice')");
    }
}