package run.halo.app.core.endpoint.console;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class CategoryEndpointTest {

    @Mock
    private CategoryConsoleService categoryConsoleService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var endpoint = new CategoryEndpoint(categoryConsoleService);
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void listTree() {
        when(categoryConsoleService.listTree())
                .thenReturn(Mono.just(List.of(new CategoryTreeNode(category("root"), List.of()))));

        webTestClient
                .get()
                .uri("/categories/-/tree")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].category.metadata.name")
                .isEqualTo("root")
                .jsonPath("$[0].children.length()")
                .isEqualTo(0);
    }

    @Test
    void updatePosition() {
        when(categoryConsoleService.updatePosition(eq("child"), eq(new CategoryPositionRequest("root", null))))
                .thenReturn(Mono.just(List.of(new CategoryTreeNode(
                        category("root"), List.of(new CategoryTreeNode(category("child"), List.of()))))));

        webTestClient
                .put()
                .uri("/categories/{name}/position", "child")
                .bodyValue(new CategoryPositionRequest("root", null))
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].category.metadata.name")
                .isEqualTo("root")
                .jsonPath("$[0].children[0].category.metadata.name")
                .isEqualTo("child");
    }

    @Test
    void updatePositionRequiresBody() {
        webTestClient
                .put()
                .uri("/categories/{name}/position", "child")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private static Category category(String name) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2024-01-01T00:00:00Z"));
        category.setMetadata(metadata);

        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(0);
        category.setSpec(spec);
        return category;
    }
}
