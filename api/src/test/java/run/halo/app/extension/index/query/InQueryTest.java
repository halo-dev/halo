package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InQuery}.
 *
 * @author guqing
 * @since 2.17.0
 */
class InQueryTest {

    @Test
    void testToString() {
        var inQuery = new InQuery("name", Set.of("Alice", "Bob"));
        assertThat(inQuery.toString()).isEqualTo("name IN ('Bob', 'Alice')");
    }
}