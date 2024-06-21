package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
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
        var values = new LinkedHashSet<String>();
        values.add("Alice");
        values.add("Bob");
        var inQuery = new InQuery("name", values);
        assertThat(inQuery.toString()).isEqualTo("name IN ('Alice', 'Bob')");
    }
}