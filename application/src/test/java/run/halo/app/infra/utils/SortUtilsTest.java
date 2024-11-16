package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SortUtils}.
 *
 * @author guqing
 * @since 2.19.0
 */
class SortUtilsTest {

    @Test
    void resolve() {
        // null case
        assertThat(SortUtils.resolve(null).isUnsorted()).isTrue();

        // multiple sort and directions
        var str = List.of("name,asc", "age,desc");
        var sort = SortUtils.resolve(str);
        assertThat(sort.toString()).isEqualTo("name: ASC,age: DESC");

        // missing direction
        str = List.of("name");
        sort = SortUtils.resolve(str);
        assertThat(sort.toString()).isEqualTo("name: ASC");

        // whitespace in direction
        assertThatThrownBy(() -> SortUtils.resolve(List.of("name, desc")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Direction must not contain whitespace");
    }
}