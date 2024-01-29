package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.index.query.QueryIndexViewImpl;

/**
 * Tests for {@link PostQuery}.
 *
 * @author guqing
 * @since 2.6.0
 */
@ExtendWith(MockitoExtension.class)
class PostQueryTest {

    @Test
    void userScopedQueryTest() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        MockServerRequest request = MockServerRequest.builder()
            .queryParams(multiValueMap)
            .exchange(mock(ServerWebExchange.class))
            .build();

        PostQuery postQuery = new PostQuery(request, "faker");

        var listOptions = postQuery.toListOptions();
        assertThat(listOptions).isNotNull();
        assertThat(listOptions.getFieldSelector()).isNotNull();
        var nameEntry =
            (Collection<Map.Entry<String, String>>) List.of(Map.entry("metadata.name", "faker"));
        var entry = (Collection<Map.Entry<String, String>>) List.of(Map.entry("faker", "faker"));
        var indexView =
            new QueryIndexViewImpl(Map.of("spec.owner", entry, "metadata.name", nameEntry));
        assertThat(listOptions.getFieldSelector().query().matches(indexView))
            .containsExactly("faker");

        entry = List.of(Map.entry("another-faker", "user1"));
        indexView = new QueryIndexViewImpl(Map.of("spec.owner", entry, "metadata.name", nameEntry));
        assertThat(listOptions.getFieldSelector().query().matches(indexView)).isEmpty();
    }
}
