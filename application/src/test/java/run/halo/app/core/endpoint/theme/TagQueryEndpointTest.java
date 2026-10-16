package run.halo.app.core.endpoint.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springdoc.core.utils.Constants.OPERATION_ATTRIBUTE;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.visitor.RouterFunctionVisitor;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;

@ExtendWith(MockitoExtension.class)
class TagQueryEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private TagFinder tagFinder;

    @Mock
    private PostPublicQueryService postPublicQueryService;

    @InjectMocks
    private TagQueryEndpoint endpoint;

    @Test
    void postsResponseSchemaIncludesPaginationAndPostItems() {
        var visitor = new RouterFunctionVisitor();
        endpoint.endpoint().accept(visitor);
        var route = visitor.getRouterFunctionDatas().stream()
                .filter(data -> data.getPath().equals("/tags/{name}/posts"))
                .findFirst()
                .orElseThrow();
        var operation = ((Builder) route.getAttributes().get(OPERATION_ATTRIBUTE)).build();
        var responseType =
                operation.getOperation().responses()[0].content()[0].schema().implementation();
        Schema<?> schema =
                ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(responseType)).schema;

        assertThat(schema.getProperties()).containsKeys("page", "size", "total", "items");
        assertThat(schema.getProperties().get("items").getType()).isEqualTo("array");
        assertThat(schema.getProperties().get("items").getItems().get$ref())
                .isEqualTo("#/components/schemas/ListedPostVo");
    }

    @Test
    void listPostsByTagNameReturnsPaginatedPosts() {
        var metadata = new Metadata();
        metadata.setName("test-post");
        var post = ListedPostVo.builder().metadata(metadata).build();
        when(postPublicQueryService.list(any(), any()))
                .thenReturn(Mono.just(new ListResult<>(1, 10, 1, List.of(post))));

        WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .build()
                .get()
                .uri("/tags/test/posts?page=1&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.page")
                .isEqualTo(1)
                .jsonPath("$.size")
                .isEqualTo(10)
                .jsonPath("$.total")
                .isEqualTo(1)
                .jsonPath("$.items[0].metadata.name")
                .isEqualTo("test-post");
    }
}
