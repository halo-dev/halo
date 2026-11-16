package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.comment.CommentPermalinkService;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationProvider;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class CommentLocalizationTest {
    private static final String SUBJECT_URL = "/archives/hello%20halo?from=comments";
    private static final String COMMENT_URL = SUBJECT_URL + "#halo-comment=comment";
    private static final String REPLY_URL = COMMENT_URL + "&reply=reply";
    private static final Ref SUBJECT = Ref.of("post", Post.GVK);
    private final ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
    private final UserService users = mock(UserService.class);
    private final CounterService counters = mock(CounterService.class);
    private final CommentPermalinkService permalinks = mock(CommentPermalinkService.class);
    private final ResourceLocalizationService localization = mock(ResourceLocalizationService.class);
    private final ExtensionGetter extensions = mock(ExtensionGetter.class);
    private final ResourceLocalizationProvider provider = mock(ResourceLocalizationProvider.class);
    private final CommentPublicQueryServiceImpl service =
            new CommentPublicQueryServiceImpl(client, users, counters, permalinks, localization, extensions);
    private Comment comment;
    private Reply reply;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setMetadata(metadata("comment"));
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setOwner(owner());
        comment.getSpec().setIpAddress("192.0.2.1");
        comment.getSpec().setContent("Comment");
        comment.getSpec().setApproved(true);
        comment.getSpec().setHidden(false);
        comment.getSpec().setSubjectRef(SUBJECT);
        comment.setStatus(new Comment.CommentStatus());
        comment.getStatus().setReplyCount(3);
        comment.getStatus().setVisibleReplyCount(1);
        comment.getStatus().setPendingReplyCount(2);
        comment.getStatus().setLastReplyTime(Instant.parse("2026-01-01T00:00:00Z"));
        reply = new Reply();
        reply.setMetadata(metadata("reply"));
        reply.setSpec(new Reply.ReplySpec());
        reply.getSpec().setOwner(owner());
        reply.getSpec().setCommentName("comment");
        reply.getSpec().setContent("Reply");
        reply.getSpec().setIpAddress("192.0.2.2");
        reply.getSpec().setApproved(true);
        reply.getSpec().setHidden(false);
        when(users.hasSufficientRoles(any())).thenReturn(Mono.just(false));
        when(counters.getByName(anyString())).thenReturn(Mono.empty());
        when(client.listBy(eq(Comment.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(1, 10, 1L, List.of(comment))));
        when(client.listBy(eq(Reply.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(1, 10, 1L, List.of(reply))));
        when(client.get(Comment.class, "comment")).thenReturn(Mono.just(comment));
        when(permalinks.getPermalink(comment)).thenReturn(Mono.just(COMMENT_URL));
        when(permalinks.getSubjectUrl(SUBJECT)).thenReturn(Mono.just(SUBJECT_URL));
        when(permalinks.getSubjectUrl(null)).thenReturn(Mono.empty());
        when(localization.localize(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.localizeUrl(anyString(), any())).thenAnswer(invocation -> "/en" + invocation.getArgument(0));
    }

    @ParameterizedTest
    @EnumSource(
            value = LocalizationScope.class,
            names = {"PUBLIC_PAGE", "PUBLIC_API"})
    void publicQueriesLocalizePermalinksAndPreserveQueryAndCommentFragments(LocalizationScope scope) {
        var context = context(scope);
        service.getByName("comment")
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    assertThat(vo.getPermalink()).isEqualTo("/en" + COMMENT_URL);
                    assertThat(vo.getSpec().getIpAddress()).isEmpty();
                    assertThat(vo.getStatus().getReplyCount()).isEqualTo(1);
                    assertThat(vo.getStatus().getPendingReplyCount()).isNull();
                    assertThat(vo.getStatus().getLastReplyTime()).isNull();
                })
                .verifyComplete();
        service.list(SUBJECT, 1, 10)
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list.getTotal()).isEqualTo(1);
                    assertThat(list.getItems().getFirst().getPermalink()).isEqualTo("/en" + COMMENT_URL);
                })
                .verifyComplete();
        service.list(null, 1, 10)
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(list ->
                        assertThat(list.getItems().getFirst().getPermalink()).isEqualTo("/en" + COMMENT_URL))
                .verifyComplete();
        service.listReply("comment", 1, 10)
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(list ->
                        assertThat(list.getItems().getFirst().getPermalink()).isEqualTo("/en" + REPLY_URL))
                .verifyComplete();
        service.getReply("comment", "reply")
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    assertThat(vo.getPermalink()).isEqualTo("/en" + REPLY_URL);
                    assertThat(vo.getSpec().getIpAddress()).isEmpty();
                    assertThat(vo.getSpec().getOwner().getAnnotation("private-note"))
                            .isNull();
                })
                .verifyComplete();
        assertThat(comment.getSpec().getIpAddress()).isEqualTo("192.0.2.1");
        assertThat(comment.getSpec().getOwner().getName()).isEqualTo("writer@example.test");
        assertThat(comment.getStatus().getReplyCount()).isEqualTo(3);
        assertThat(comment.getStatus().getPendingReplyCount()).isEqualTo(2);
        assertThat(reply.getSpec().getIpAddress()).isEqualTo("192.0.2.2");
        assertThat(reply.getSpec().getOwner().getAnnotation("private-note")).isEqualTo("private");
    }

    @ParameterizedTest
    @EnumSource(
            value = LocalizationScope.class,
            names = {"CONSOLE", "INTERNAL", "SEARCH_INDEX"})
    void nonPublicScopesKeepOriginalLinks(LocalizationScope scope) {
        service.getByName("comment")
                .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context(scope)))
                .as(StepVerifier::create)
                .assertNext(vo -> assertThat(vo.getPermalink()).isEqualTo(COMMENT_URL))
                .verifyComplete();
        verifyNoInteractions(extensions, provider);
    }

    @Test
    void absentContextKeepsOriginalLinksAndDoesNotMutateSource() {
        service.getByName("comment")
                .as(StepVerifier::create)
                .assertNext(vo -> assertThat(vo.getPermalink()).isEqualTo(COMMENT_URL))
                .verifyComplete();
        assertThat(comment.getSpec().getOwner().getName()).isEqualTo("writer@example.test");
        assertThat(comment.getSpec().getIpAddress()).isEqualTo("192.0.2.1");
        verifyNoInteractions(localization, extensions, provider);
    }

    @Test
    void unavailableProviderKeepsOriginalLinks() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.empty());
        assertOriginalCommentLink();
        verifyNoInteractions(provider);
    }

    @Test
    void nullProviderResultKeepsOriginalLinks() {
        when(provider.localizeUrl(anyString(), any())).thenReturn(null);
        assertOriginalCommentLink();
    }

    @Test
    void providerFailureKeepsOriginalLinks() {
        when(provider.localizeUrl(anyString(), any())).thenThrow(new IllegalStateException("Unavailable"));
        assertOriginalCommentLink();
    }

    @Test
    void unavailableCommentDoesNotResolveReplyOrTranslation() {
        when(client.listBy(eq(Comment.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(ListResult.emptyResult()));
        service.getReply("comment", "reply")
                .contextWrite(reactorContext ->
                        reactorContext.put(LocalizationContext.class, context(LocalizationScope.PUBLIC_API)))
                .as(StepVerifier::create)
                .verifyComplete();
        verify(client, never()).listBy(eq(Reply.class), any(ListOptions.class), any(PageRequest.class));
        verifyNoInteractions(localization, extensions, provider, permalinks);
    }

    @Test
    void hiddenParentRejectsReplyListBeforeLocalization() {
        comment.getSpec().setHidden(true);
        service.listReply("comment", 1, 10)
                .contextWrite(reactorContext ->
                        reactorContext.put(LocalizationContext.class, context(LocalizationScope.PUBLIC_API)))
                .as(StepVerifier::create)
                .expectError(UnsatisfiedAttributeValueException.class)
                .verify();
        verifyNoInteractions(localization, extensions, provider, permalinks);
    }

    private void assertOriginalCommentLink() {
        service.getByName("comment")
                .contextWrite(reactorContext ->
                        reactorContext.put(LocalizationContext.class, context(LocalizationScope.PUBLIC_API)))
                .as(StepVerifier::create)
                .assertNext(vo -> assertThat(vo.getPermalink()).isEqualTo(COMMENT_URL))
                .verifyComplete();
    }

    private static LocalizationContext context(LocalizationScope scope) {
        return new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), scope);
    }

    private static Metadata metadata(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }

    private static Comment.CommentOwner owner() {
        var owner = new Comment.CommentOwner();
        owner.setKind(Comment.CommentOwner.KIND_EMAIL);
        owner.setName("writer@example.test");
        owner.setDisplayName("Writer");
        owner.setAnnotations(new HashMap<>(Map.of("private-note", "private")));
        return owner;
    }
}
