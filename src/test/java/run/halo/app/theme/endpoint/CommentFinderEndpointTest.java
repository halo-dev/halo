package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.theme.finders.CommentFinder;

/**
 * Tests for {@link CommentFinderEndpoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CommentFinderEndpointTest {
    @Mock
    private CommentFinder commentFinder;

    @InjectMocks
    private CommentFinderEndpoint commentFinderEndpoint;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToRouterFunction(commentFinderEndpoint.endpoint())
            .build();
    }

    @Test
    void listComments() {
        when(commentFinder.list(any(), anyInt(), anyInt()))
            .thenReturn(new ListResult<>(1, 10, 0, List.of()));

        Ref ref = new Ref();
        ref.setGroup("content.halo.run");
        ref.setVersion("v1alpha1");
        ref.setKind("Post");
        ref.setName("test");
        webTestClient.get()
            .uri(uriBuilder -> {
                return uriBuilder.path("/comments")
                    .queryParam("group", ref.getGroup())
                    .queryParam("version", ref.getVersion())
                    .queryParam("kind", ref.getKind())
                    .queryParam("name", ref.getName())
                    .queryParam("page", 1)
                    .queryParam("size", 10)
                    .build();
            })
            .exchange()
            .expectStatus()
            .isOk();
        ArgumentCaptor<Ref> refCaptor = ArgumentCaptor.forClass(Ref.class);
        verify(commentFinder, times(1)).list(refCaptor.capture(), eq(1), eq(10));
        Ref value = refCaptor.getValue();
        assertThat(value).isEqualTo(ref);
    }

    @Test
    void getComment() {
        when(commentFinder.getByName(any()))
            .thenReturn(null);

        webTestClient.get()
            .uri("/comments/test-comment")
            .exchange()
            .expectStatus()
            .isOk();

        verify(commentFinder, times(1)).getByName(eq("test-comment"));
    }

    @Test
    void listCommentReplies() {
        when(commentFinder.listReply(any(), anyInt(), anyInt()))
            .thenReturn(new ListResult<>(2, 20, 0, List.of()));

        webTestClient.get()
            .uri(uriBuilder -> {
                return uriBuilder.path("/comments/test-comment/reply")
                    .queryParam("page", 2)
                    .queryParam("size", 20)
                    .build();
            })
            .exchange()
            .expectStatus()
            .isOk();

        verify(commentFinder, times(1)).listReply(eq("test-comment"), eq(2), eq(20));
    }
}