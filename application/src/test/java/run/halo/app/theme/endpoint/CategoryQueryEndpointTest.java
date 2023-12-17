package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.ListedPostVo;

/**
 * Tests for {@link CategoryQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryQueryEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private PostPublicQueryService postPublicQueryService;
    private CategoryQueryEndpoint endpoint;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        endpoint = new CategoryQueryEndpoint(client, postPublicQueryService);
        RouterFunction<ServerResponse> routerFunction = endpoint.endpoint();
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void listCategories() {
        ListResult<Category> listResult = new ListResult<>(List.of());
        when(client.list(eq(Category.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(listResult));

        webTestClient.get()
            .uri("/categories?page=1&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.total").isEqualTo(listResult.getTotal())
            .jsonPath("$.items").isArray();
    }

    @Test
    void getByName() {
        Category category = new Category();
        category.setMetadata(new Metadata());
        category.getMetadata().setName("test");
        when(client.get(eq(Category.class), eq("test"))).thenReturn(Mono.just(category));

        webTestClient.get()
            .uri("/categories/test")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo(category.getMetadata().getName());
    }

    @Test
    void listPostsByCategoryName() {
        ListResult<ListedPostVo> listResult = new ListResult<>(List.of());
        when(postPublicQueryService.list(anyInt(), anyInt(), any(), any()))
            .thenReturn(Mono.just(listResult));

        webTestClient.get()
            .uri("/categories/test/posts?page=1&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.total").isEqualTo(listResult.getTotal())
            .jsonPath("$.items").isArray();
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.content.halo.run/v1alpha1");
    }
}