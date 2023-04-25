package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * Tests for {@link SinglePageQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageQueryEndpointTest {

    @Mock
    private SinglePageFinder singlePageFinder;

    @InjectMocks
    private SinglePageQueryEndpoint endpoint;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void listSinglePages() {
        ListedSinglePageVo test = ListedSinglePageVo.builder()
            .metadata(metadata("test"))
            .spec(new SinglePage.SinglePageSpec())
            .build();

        ListResult<ListedSinglePageVo> pageResult = new ListResult<>(List.of(test));

        when(singlePageFinder.list(anyInt(), anyInt(), any(), any()))
            .thenReturn(Mono.just(pageResult));

        webTestClient.get()
            .uri("/singlepages?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.total").isEqualTo(1)
            .jsonPath("$.items[0].metadata.name").isEqualTo("test");

        verify(singlePageFinder).list(eq(0), eq(10), any(), any());
    }

    @Test
    void getByName() {
        SinglePageVo singlePage = SinglePageVo.builder()
            .metadata(metadata("fake-page"))
            .spec(new SinglePage.SinglePageSpec())
            .build();

        when(singlePageFinder.getByName(eq("fake-page")))
            .thenReturn(Mono.just(singlePage));

        webTestClient.get()
            .uri("/singlepages/fake-page")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo("fake-page");

        verify(singlePageFinder).getByName("fake-page");
    }

    Metadata metadata(String name) {
        Metadata metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.content.halo.run/v1alpha1");
    }
}