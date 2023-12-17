package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * Tests for {@link PostQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class PostQueryEndpointTest {

    private WebTestClient webClient;

    @Mock
    private PostFinder postFinder;

    @Mock
    private PostPublicQueryService postPublicQueryService;

    @InjectMocks
    private PostQueryEndpoint endpoint;

    @BeforeEach
    public void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .build();
    }

    @Test
    public void listPosts() {
        ListResult<ListedPostVo> result = new ListResult<>(List.of());
        when(postPublicQueryService.list(anyInt(), anyInt(), any(), any()))
            .thenReturn(Mono.just(result));

        webClient.get().uri("/posts")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.items").isArray();

        verify(postPublicQueryService).list(anyInt(), anyInt(), any(), any());
    }

    @Test
    public void getPostByName() {
        Metadata metadata = new Metadata();
        metadata.setName("test");
        PostVo post = PostVo.builder()
            .metadata(metadata)
            .build();
        when(postFinder.getByName(anyString())).thenReturn(Mono.just(post));

        webClient.get().uri("/posts/{name}", "test")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo("test");

        verify(postFinder).getByName(anyString());
    }

    @Test
    public void testGetPostNavigationByName() {
        Metadata metadata = new Metadata();
        metadata.setName("test");
        NavigationPostVo navigation = NavigationPostVo.builder()
            .current(PostVo.builder().metadata(metadata).build())
            .build();
        when(postFinder.cursor(anyString()))
            .thenReturn(Mono.just(navigation));

        webClient.get().uri("/posts/{name}/navigation", "test")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.current.metadata.name").isEqualTo("test");

        verify(postFinder).cursor(anyString());
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.content.halo.run/v1alpha1");
    }
}