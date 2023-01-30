package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Tests for {@link RadixRouterTree}.
 *
 * @author guqing
 * @since 2.0.0
 */
class RadixRouterTreeTest {

    @Test
    void pathToFind() throws URISyntaxException {
        MockServerRequest request =
            MockServerRequest.builder().uri(new URI("/archives"))
                .method(HttpMethod.GET).build();
        String path = RadixRouterTree.pathToFind(request);
        assertThat(path).isEqualTo("/archives");

        request = MockServerRequest.builder().uri(new URI("/archives/"))
            .method(HttpMethod.GET).build();
        assertThat(RadixRouterTree.pathToFind(request)).isEqualTo("/archives");

        request = MockServerRequest.builder().uri(new URI("/archives/page/1"))
            .method(HttpMethod.GET).build();
        assertThat(RadixRouterTree.pathToFind(request)).isEqualTo("/archives");

        request = MockServerRequest.builder().uri(new URI("/"))
            .method(HttpMethod.GET).build();
        assertThat(RadixRouterTree.pathToFind(request)).isEqualTo("/");

        request = MockServerRequest.builder().uri(new URI("/"))
            .queryParam("p", "fake-post")
            .method(HttpMethod.GET).build();
        assertThat(RadixRouterTree.pathToFind(request)).isEqualTo("/?p=fake-post");
    }

    @Test
    void shouldInsertKeyWithPercentSign() {
        var tree = new RadixRouterTree();
        tree.insert("/1%1", request -> ServerResponse.ok().build());
    }
}