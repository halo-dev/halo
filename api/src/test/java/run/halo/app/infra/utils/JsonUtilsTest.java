package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

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
}
