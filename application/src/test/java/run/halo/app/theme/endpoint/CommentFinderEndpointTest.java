package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentRequest;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.comment.ReplyRequest;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.CommentPublicQueryService;

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

    @Mock
    private CommentPublicQueryService commentPublicQueryService;

    @Mock
    private CommentService commentService;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Mock
    private ReplyService replyService;

    @InjectMocks
    private CommentFinderEndpoint commentFinderEndpoint;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        lenient().when(environmentFetcher.fetchComment()).thenReturn(Mono.empty());
        webTestClient = WebTestClient
            .bindToRouterFunction(commentFinderEndpoint.endpoint())
            .build();
    }

    @Test
    void listComments() {
        when(commentPublicQueryService.list(any(), anyInt(), anyInt(), any()))
            .thenReturn(Mono.just(new ListResult<>(1, 10, 0, List.of())));

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
        verify(commentPublicQueryService, times(1)).list(refCaptor.capture(), eq(1), eq(10), any());
        Ref value = refCaptor.getValue();
        assertThat(value).isEqualTo(ref);
    }

    @Test
    void getComment() {
        when(commentPublicQueryService.getByName(any()))
            .thenReturn(null);

        webTestClient.get()
            .uri("/comments/test-comment")
            .exchange()
            .expectStatus()
            .isOk();

        verify(commentPublicQueryService, times(1)).getByName(eq("test-comment"));
    }

    @Test
    void listCommentReplies() {
        when(commentPublicQueryService.listReply(any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(new ListResult<>(2, 20, 0, List.of())));

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/comments/test-comment/reply")
                .queryParam("page", 2)
                .queryParam("size", 20)
                .build())
            .exchange()
            .expectStatus()
            .isOk();

        verify(commentPublicQueryService, times(1)).listReply(eq("test-comment"), eq(2), eq(20));
    }

    @Test
    void createComment() {
        when(commentService.create(any())).thenReturn(Mono.empty());

        final CommentRequest commentRequest = new CommentRequest();
        Ref ref = new Ref();
        ref.setGroup("content.halo.run");
        ref.setVersion("v1alpha1");
        ref.setKind("Post");
        ref.setName("test-post");
        commentRequest.setSubjectRef(ref);
        commentRequest.setContent("content");
        commentRequest.setRaw("raw");
        commentRequest.setAllowNotification(false);
        webTestClient.post()
            .uri("/comments")
            .bodyValue(commentRequest)
            .exchange()
            .expectStatus()
            .isOk();

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentService, times(1)).create(captor.capture());
        Comment value = captor.getValue();
        assertThat(value.getSpec().getIpAddress()).isNotNull();
        assertThat(value.getSpec().getUserAgent()).isNotNull();
        assertThat(value.getSpec().getSubjectRef()).isEqualTo(ref);
    }

    @Test
    void createReply() {
        when(replyService.create(any(), any())).thenReturn(Mono.empty());

        final ReplyRequest replyRequest = new ReplyRequest();
        replyRequest.setRaw("raw");
        replyRequest.setContent("content");
        replyRequest.setAllowNotification(true);

        webTestClient.post()
            .uri("/comments/test-comment/reply")
            .bodyValue(replyRequest)
            .exchange()
            .expectStatus()
            .isOk();

        ArgumentCaptor<Reply> captor = ArgumentCaptor.forClass(Reply.class);
        verify(replyService, times(1)).create(eq("test-comment"), captor.capture());
        Reply value = captor.getValue();
        assertThat(value.getSpec().getIpAddress()).isNotNull();
        assertThat(value.getSpec().getUserAgent()).isNotNull();
        assertThat(value.getSpec().getQuoteReply()).isNull();
    }
}