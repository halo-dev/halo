package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link JacksonUtils}.
 *
 * @since 2.28.0
 */
class JacksonUtilsTest {

    @ParameterizedTest
    @MethodSource("mergePatchCases")
    void shouldApplyMergePatch(String target, String patch, String expected) {
        var targetNode = JacksonUtils.MAPPER.readTree(target);
        var patchNode = JacksonUtils.MAPPER.readTree(patch);

        var result = JacksonUtils.mergePatch(targetNode, patchNode);

        assertEquals(JacksonUtils.MAPPER.readTree(expected), result);
    }

    @Test
    void shouldNotMutateTarget() {
        var target = JacksonUtils.MAPPER.readTree("{\"a\":\"b\",\"nested\":{\"c\":\"d\"}}");
        var patch = JacksonUtils.MAPPER.readTree("{\"a\":\"z\",\"nested\":{\"c\":null,\"e\":\"f\"}}");

        JacksonUtils.mergePatch(target, patch);

        assertEquals(JacksonUtils.MAPPER.readTree("{\"a\":\"b\",\"nested\":{\"c\":\"d\"}}"), target);
    }

    @Test
    void shouldCreateObjectWhenTargetIsNull() {
        var patch = JacksonUtils.MAPPER.readTree("{\"a\":1}");

        assertEquals(patch, JacksonUtils.mergePatch(null, patch));
    }

    static Stream<Arguments> mergePatchCases() {
        // Cases from RFC 7386, section 3, plus some regression cases.
        return Stream.of(
                Arguments.of("{\"a\":\"b\"}", "{\"a\":\"c\"}", "{\"a\":\"c\"}"),
                Arguments.of("{\"a\":\"b\"}", "{\"b\":\"c\"}", "{\"a\":\"b\",\"b\":\"c\"}"),
                Arguments.of("{\"a\":\"b\"}", "{\"a\":null}", "{}"),
                Arguments.of("{\"a\":\"b\",\"b\":\"c\"}", "{\"a\":null}", "{\"b\":\"c\"}"),
                Arguments.of("{\"a\":[\"b\"]}", "{\"a\":\"c\"}", "{\"a\":\"c\"}"),
                Arguments.of("{\"a\":\"c\"}", "{\"a\":[\"b\"]}", "{\"a\":[\"b\"]}"),
                Arguments.of("{\"a\":{\"b\":\"c\"}}", "{\"a\":{\"b\":\"d\",\"c\":null}}", "{\"a\":{\"b\":\"d\"}}"),
                Arguments.of("{\"a\":[{\"b\":\"c\"}]}", "{\"a\":[1]}", "{\"a\":[1]}"),
                Arguments.of("[\"a\",\"b\"]", "[\"c\",\"d\"]", "[\"c\",\"d\"]"),
                Arguments.of("{\"a\":\"b\"}", "[\"c\"]", "[\"c\"]"),
                Arguments.of("{\"a\":\"foo\"}", "null", "null"),
                Arguments.of("{\"a\":\"foo\"}", "\"bar\"", "\"bar\""),
                Arguments.of("{\"e\":null}", "{\"a\":1}", "{\"e\":null,\"a\":1}"),
                Arguments.of("[1,2]", "{\"a\":\"b\",\"c\":null}", "{\"a\":\"b\"}"),
                Arguments.of("{}", "{\"a\":{\"bb\":{\"ccc\":null}}}", "{\"a\":{\"bb\":{}}}"));
    }
}
