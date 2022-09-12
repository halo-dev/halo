package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Tests for {@link JsonPropertyAccessor}.
 *
 * @author guqing
 * @since 2.0.0
 */
class JsonPropertyAccessorTest {
    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        context.addPropertyAccessor(new JsonPropertyAccessor());
        ConverterRegistry converterRegistry =
            (ConverterRegistry) DefaultConversionService.getSharedInstance();
        converterRegistry.addConverter(new JsonNodeWrapperToJsonNodeConverter());
    }

    @Test
    public void testSimpleLookup() throws Exception {
        JsonNode json = mapper.readTree("{\"foo\": \"bar\"}");
        String value = evaluate(json, "foo", String.class);
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).isEqualTo("bar");
        JsonNode json2 = mapper.readTree("{\"foo\": \"bar\"}");
        String value2 = evaluate(json2, "foo", String.class);
        assertThat(value2).isInstanceOf(String.class);
        assertThat(value.equals(value2)).isTrue();
        assertThat(value2.hashCode()).isEqualTo(value.hashCode());
    }

    @Test
    public void testTextNode() throws Exception {
        JsonNode json = mapper.readTree("\"foo\"");
        String result = evaluate(json, "#root", String.class);
        assertThat(result).isEqualTo("\"foo\"");
    }

    @Test
    public void testMissingProperty() throws Exception {
        JsonNode json = mapper.readTree("{\"foo\": \"bar\"}");
        assertThat(evaluate(json, "fizz", String.class)).isNull();
    }

    @Test
    public void testArrayLookup() throws Exception {
        ArrayNode json = (ArrayNode) mapper.readTree("[3, 4, 5]");
        // Have to wrap the root array because ArrayNode itself is not a List
        Integer actual = evaluate(JsonPropertyAccessor.wrap(json), "[1]", Integer.class);
        assertThat(actual).isEqualTo(4);
    }

    @Test
    public void testArrayNegativeIndex() throws Exception {
        JsonNode json = mapper.readTree("{\"foo\":[3, 4, 5]}");
        // help access json list items with json-path negative index
        assertThat(evaluate(json, "foo[-1]", Integer.class)).isEqualTo(5);
    }

    @Test
    public void testArrayIndexOutOfBounds() throws Exception {
        JsonNode json = mapper.readTree("{\"foo\":[3, 4, 5]}");
        assertThatExceptionOfType(SpelEvaluationException.class)
            .isThrownBy(() -> evaluate(json, "foo[3]", Object.class));
    }

    @Test
    public void testArrayLookupWithStringIndex() throws Exception {
        JsonNode json = mapper.readTree("[3, 4, 5]");
        Integer actual = evaluate(json, "['1']", Integer.class);
        assertThat(actual).isEqualTo(4);
    }

    @Test
    public void testNestedArrayConstruct() throws Exception {
        ArrayNode json = (ArrayNode) mapper.readTree("[[3], [4, 5], []]");
        // JsonNode actual = evaluate("1.1", json, JsonNode.class); // Does not work
        Object actual = evaluate(JsonPropertyAccessor.wrap(json), "[1][1]", Object.class);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testNestedArrayConstructWithStringIndex() throws Exception {
        Object json = mapper.readTree("[[3], [4, 5], []]");
        Object actual = evaluate(json, "['1']['1']", Object.class);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testArrayProjectionResult() throws Exception {
        Object json = mapper.readTree(
            "{\"foo\": {\"bar\": [ { \"fizz\": 5, \"buzz\": 6 }, {\"fizz\": 7}, {\"fizz\": 8} ] }"
                + " }");
        // Filter the bar array to return only the fizz value of each element (to prove that SPeL
        // considers bar
        // an array/list)
        List<?> actualArray = evaluate(json, "foo.bar.![fizz]", List.class);
        assertThat(actualArray).hasSize(3);
        assertThat(evaluate(actualArray, "[0]", Object.class)).isEqualTo(5);
        assertThat(evaluate(actualArray, "[1]", Object.class)).isEqualTo(7);
        assertThat(evaluate(actualArray, "[2]", Object.class)).isEqualTo(8);
    }

    @Test
    public void testFilterOnArraySelection() throws Exception {
        Object json = mapper.readTree(
            "{\"foo\": {\"bar\": [ { \"fizz\": 5, \"buzz\": 6 }, {\"fizz\": 7}, {\"fizz\": 8} ] }"
                + " }");

        // Filter bar objects so that none match
        List<?> actualArray = evaluate(json, "foo.bar.?[fizz == 0]", List.class);
        assertThat(actualArray).hasSize(0);

        // Filter bar objects so that one match
        actualArray = evaluate(json, "foo.bar.?[fizz == 8]", List.class);
        assertThat(actualArray).hasSize(1);
        assertThat(((JsonPropertyAccessor.ComparableJsonNode) actualArray.get(0)).getRealNode())
            .isEqualTo(mapper.readTree("{\"fizz\": 8}"));

        // Filter bar objects so several match
        actualArray = evaluate(json, "foo.bar.?[fizz > 6]", List.class);
        assertThat(actualArray).hasSize(2);
        assertThat(((JsonPropertyAccessor.ComparableJsonNode) actualArray.get(0)).getRealNode())
            .isEqualTo(mapper.readTree("{\"fizz\": 7}"));
        assertThat(((JsonPropertyAccessor.ComparableJsonNode) actualArray.get(1)).getRealNode())
            .isEqualTo(mapper.readTree("{\"fizz\": 8}"));
    }

    @Test
    public void testNestedHashConstruct() throws Exception {
        Object json = mapper.readTree("{\"foo\": {\"bar\": 4, \"fizz\": 5} }");
        Object actual = evaluate(json, "foo.fizz", Object.class);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testImplicitStringConversion() {
        String json = "{\"foo\": {\"bar\": 4, \"fizz\": 5} }";
        Object actual = evaluate(json, "foo.fizz", Object.class);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testSelectorAccess() {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";
        Object actual = evaluate(json, "property.^[name == 'value1'].name", Object.class);
        assertThat(actual).isEqualTo("value1");
    }

    @Test
    public void testJsonGetValueConversionAsJsonNode() throws Exception {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";

        // use JsonNode conversion
        Object node = evaluate(json, "property.^[name == 'value1']", JsonNode.class);
        assertThat(node).isInstanceOf(JsonNode.class);
        assertThat(((JsonNode) node)).isEqualTo(mapper.readTree("{\"name\":\"value1\"}"));
    }

    @Test
    public void testJsonGetValueConversionAsObjectNode() throws Exception {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";

        // use ObjectNode conversion
        Object node = evaluate(json, "property.^[name == 'value1']", JsonNode.class);
        assertThat(node).isInstanceOf(ObjectNode.class);
        assertThat(((ObjectNode) node)).isEqualTo(mapper.readTree("{\"name\":\"value1\"}"));
    }

    @Test
    public void testJsonGetValueConversionAsArrayNode() throws Exception {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";

        // use ArrayNode conversion
        Object node = evaluate(json, "property", ArrayNode.class);
        assertThat(node).isInstanceOf(ArrayNode.class);
        assertThat(((ArrayNode) node)).isEqualTo(
            mapper.readTree("[{\"name\":\"value1\"},{\"name\":\"value2\"}]"));
    }

    @Test
    public void testJsonGetValueConversionAsString() {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";

        // use ArrayNode conversion
        Object node = evaluate(json, "#root", String.class);
        assertThat(node).isInstanceOf(String.class);
        assertThat(((String) node)).isEqualTo(
            "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}");
    }

    @Test
    public void testSelectorComparingJsonNode() throws Exception {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}], "
            + "\"property2\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";
        Object actual = evaluate(json, "property[0] eq property2[0]", Object.class);
        assertThat(actual).isEqualTo(true);
    }

    @Test
    public void testSelectorComparingArrayNode() throws Exception {
        String json = "{\"property\":[{\"name\":\"value1\"},{\"name\":\"value2\"}], "
            + "\"property2\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";
        Object actual = evaluate(json, "property eq property2", Object.class);
        assertThat(actual).isEqualTo(true);
    }

    @Test
    public void testUnsupportedString() {
        String xml = "<what>?</what>";
        assertThatExceptionOfType(SpelEvaluationException.class)
            .isThrownBy(() -> evaluate(xml, "what", Object.class));
    }

    @Test
    public void testUnsupportedJson() {
        String json = "\"literal\"";
        assertThat(evaluate(json, "foo", Object.class)).isNull();
    }

    @Test
    public void testNoNullPointerWithCachedReadAccessor() throws Exception {
        Expression expression = parser.parseExpression("foo");
        Object json = mapper.readTree("{\"foo\": \"bar\"}");
        Object value = expression.getValue(this.context, json);
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).isEqualTo("bar");
        Object json2 = mapper.readTree("{}");
        Object value2 = expression.getValue(this.context, json2);
        assertThat(value2).isNull();
    }

    private <T> T evaluate(Object target, String expression, Class<T> expectedType) {
        return parser.parseExpression(expression).getValue(context, target, expectedType);
    }
}