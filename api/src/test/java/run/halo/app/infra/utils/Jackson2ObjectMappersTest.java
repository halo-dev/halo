package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Jackson2ObjectMappers}.
 *
 * <p>The configuration of the mapper is the contract of the JSON persisted in the extension store, so these tests pin
 * it down.
 *
 * @author johnniang
 * @since 2.28.0
 */
class Jackson2ObjectMappersTest {

    final ObjectMapper mapper = Jackson2ObjectMappers.shared();

    @Test
    void shouldWriteDatesAsStrings() throws JsonProcessingException {
        var sample = new Sample(Instant.parse("2026-11-21T03:04:05Z"), null, null, null);

        assertThat(mapper.writeValueAsString(sample)).isEqualTo("{\"instant\":\"2026-11-21T03:04:05Z\"}");
    }

    @Test
    void shouldWriteEnumsUsingToString() throws JsonProcessingException {
        var sample = new Sample(null, null, Level.HIGH, null);

        assertThat(mapper.writeValueAsString(sample)).isEqualTo("{\"level\":\"high\"}");
    }

    @Test
    void shouldWriteBigDecimalAsPlain() throws JsonProcessingException {
        var sample = new Sample(null, new BigDecimal("1E+3"), null, null);

        assertThat(mapper.writeValueAsString(sample)).isEqualTo("{\"amount\":1000}");
    }

    @Test
    void shouldOmitNullProperties() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(new Sample(null, null, null, null)))
                .isEqualTo("{}");
    }

    @Test
    void shouldReadUnknownProperties() throws JsonProcessingException {
        var json = "{\"instant\":\"2026-11-21T03:04:05Z\",\"unknown\":\"ignored\"}";

        assertThat(mapper.readValue(json, Sample.class))
                .isEqualTo(new Sample(Instant.parse("2026-11-21T03:04:05Z"), null, null, null));
    }

    record Sample(Instant instant, BigDecimal amount, Level level, String note) {}

    enum Level {
        HIGH;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
