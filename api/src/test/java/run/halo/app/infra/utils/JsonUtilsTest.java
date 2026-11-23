package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JsonUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JsonUtilsTest {

    @Test
    public void serializerTime() {
        Instant now = Instant.now();
        String instantStr = JsonUtils.objectToJson(now);
        assertThat(instantStr).isNotNull();

        String localDateTimeStr = JsonUtils.objectToJson(LocalDateTime.now());
        assertThat(localDateTimeStr).isNotNull();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void deserializerArrayString() {
        String s = "[\"hello\", \"world\"]";
        List list = JsonUtils.jsonToObject(s, List.class);
        assertThat(list).isEqualTo(List.of("hello", "world"));
    }

    @Test
    public void serializerShouldWriteDatesAsStrings() throws JsonProcessingException {
        var sample = new Sample(Instant.parse("2026-11-21T03:04:05Z"), null, null);

        assertThat(JsonUtils.objectToJson(sample)).isEqualTo("{\"instant\":\"2026-11-21T03:04:05Z\"}");
    }

    @Test
    public void serializerShouldWriteEnumsUsingToString() throws JsonProcessingException {
        var sample = new Sample(null, Level.HIGH, null);

        assertThat(JsonUtils.objectToJson(sample)).isEqualTo("{\"level\":\"high\"}");
    }

    @Test
    public void serializerShouldWriteBigDecimalAsPlain() throws JsonProcessingException {
        var sample = new Sample(null, null, new BigDecimal("1E+3"));

        assertThat(JsonUtils.objectToJson(sample)).isEqualTo("{\"amount\":1000}");
    }

    @Test
    public void serializerShouldOmitNullProperties() throws JsonProcessingException {
        assertThat(JsonUtils.objectToJson(new Sample(null, null, null))).isEqualTo("{}");
    }

    @Test
    public void deserializerShouldIgnoreUnknownProperties() {
        var json = "{\"instant\":\"2026-11-21T03:04:05Z\",\"unknown\":\"ignored\"}";

        assertThat(JsonUtils.jsonToObject(json, Sample.class))
                .isEqualTo(new Sample(Instant.parse("2026-11-21T03:04:05Z"), null, null));
    }

    record Sample(Instant instant, Level level, BigDecimal amount) {}

    enum Level {
        HIGH;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
