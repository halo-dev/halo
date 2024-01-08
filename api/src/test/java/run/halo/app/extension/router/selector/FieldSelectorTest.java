package run.halo.app.extension.router.selector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FieldSelector}.
 *
 * @author guqing
 * @since 2.12.0
 */
class FieldSelectorTest {

    @Test
    void testEq() {
        var fieldSelector = FieldSelector.builder()
            .eq("name", "guqing").build();
        assertThat(fieldSelector.test(key -> "guqing")).isTrue();
        assertThat(fieldSelector.test(key -> "halo")).isFalse();
    }

    @Test
    void testNotEq() {
        var fieldSelector = FieldSelector.builder()
            .notEq("name", "guqing").build();
        assertThat(fieldSelector.test(key -> "guqing")).isFalse();
        assertThat(fieldSelector.test(key -> "halo")).isTrue();
    }

    @Test
    void testIn() {
        var fieldSelector = FieldSelector.builder()
            .in("name", "guqing", "guqing1").build();
        assertThat(fieldSelector.test(key -> "guqing")).isTrue();
        assertThat(fieldSelector.test(key -> "halo")).isFalse();
        assertThat(fieldSelector.test(key -> "blog")).isFalse();
    }

    @Test
    void testNotIn() {
        var fieldSelector = FieldSelector.builder()
            .notIn("name", "guqing", "guqing1").build();
        assertThat(fieldSelector.test(key -> "guqing")).isFalse();
        assertThat(fieldSelector.test(key -> "halo")).isTrue();
        assertThat(fieldSelector.test(key -> "blog")).isTrue();
    }

    @Test
    void testExists() {
        var fieldSelector = FieldSelector.builder()
            .exists("name").build();
        assertThat(fieldSelector.test(key -> "guqing")).isTrue();
        assertThat(fieldSelector.test(key -> "halo")).isTrue();
        assertThat(fieldSelector.test(key -> "blog")).isTrue();
    }

    @Test
    void testNotExists() {
        var fieldSelector = FieldSelector.builder()
            .notExists("name").build();
        assertThat(fieldSelector.test(key -> "guqing")).isFalse();
        assertThat(fieldSelector.test(key -> "halo")).isFalse();
        assertThat(fieldSelector.test(key -> "blog")).isFalse();
    }

    @Test
    void testAnd() {
        var fieldSelector = FieldSelector.builder()
            .eq("name", "guqing")
            .and(FieldSelector.builder().eq("age", "18").build().getMatcher())
            .build();
        assertThat(fieldSelector.test(key -> "guqing")).isFalse();
        assertThat(fieldSelector.test(key -> "halo")).isFalse();
        assertThat(fieldSelector.test(key -> "18")).isFalse();
        assertThat(fieldSelector.test(key -> "guqing18")).isFalse();
        assertThat(fieldSelector.test(key -> {
            var map = Map.of("name", "guqing", "age", "18");
            return map.get(key);
        })).isTrue();
        assertThat(fieldSelector.test(key -> {
            var map = Map.of("name", "guqing", "age", "19");
            return map.get(key);
        })).isFalse();
    }

    @Test
    void testOr() {
        var fieldSelector = FieldSelector.builder()
            .eq("name", "guqing")
            .or(FieldSelector.builder().eq("age", "18").build().getMatcher())
            .build();
        assertThat(fieldSelector.test(key -> "guqing")).isTrue();
        assertThat(fieldSelector.test(key -> "halo")).isFalse();
        assertThat(fieldSelector.test(key -> "blog")).isFalse();
        assertThat(fieldSelector.test(key -> "18")).isTrue();
        assertThat(fieldSelector.test(key -> "guqing18")).isFalse();
        assertThat(fieldSelector.test(key -> {
            var map = Map.of("name", "guqing", "age", "18");
            return map.get(key);
        })).isTrue();
    }
}