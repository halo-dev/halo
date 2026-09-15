package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.*;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.finders.CommentPublicQueryService;

@DirtiesContext
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "halo.security.basic-auth.disabled=false",
            "halo.external-url=https://configured.example.test",
            "halo.work-dir=${java.io.tmpdir}/halo-comment-permalinks-test"
        })
class CommentPermalinkIntegrationTest {
    private static final String SUBJECT_URL = "/archives/hello%20halo?lang=zh";
    private static final String COMMENT_URL = SUBJECT_URL + "#halo-comment=permalink-comment";
    private static final String REPLY_URL = COMMENT_URL + "&reply=permalink-reply";
    private static final String REPLY_API = "/apis/api.halo.run/v1alpha1/comments/permalink-comment/reply/";

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    ReactiveExtensionStoreClient storeClient;

    @Autowired
    SchemeManager schemeManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CommentPublicQueryService publicQueryService;

    @Autowired
    PostPermalinkPolicy postPermalinkPolicy;

    @Autowired
    HaloProperties haloProperties;

    @MockitoSpyBean
    CommentPermalinkService permalinkService;

    @LocalServerPort
    int port;

    private final List<Extension> created = new ArrayList<>();
    private WebTestClient http;
    private Comment comment;
    private Reply reply;
    private Post post;

    @BeforeEach
    void setUp() {
        http = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(20))
                .build();
        post = new Post();
        post.setMetadata(metadata("permalink-post"));
        post.setSpec(new Post.PostSpec());
        post.getSpec().setTitle("Permalink post");
        post.getSpec().setSlug("permalink-post");
        post.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        post.getSpec().setExcerpt(new Post.Excerpt());
        post.getSpec().getExcerpt().setAutoGenerate(true);
        post.getSpec().setDeleted(false);
        post.getSpec().setPublish(true);
        post.getSpec().setPinned(false);
        post.getSpec().setAllowComment(true);
        post.getSpec().setPriority(0);
        post.getStatusOrDefault().setPermalink(SUBJECT_URL);
        post = save(post);

        comment = new Comment();
        comment.setMetadata(metadata("permalink-comment"));
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setSubjectRef(Ref.of(post));
        configure(comment.getSpec(), "thread-owner");
        comment = save(comment);

        reply = new Reply();
        reply.setMetadata(metadata("permalink-reply"));
        reply.setSpec(new Reply.ReplySpec());
        reply.getSpec().setCommentName(comment.getMetadata().getName());
        configure(reply.getSpec(), "reply-owner");
        reply = save(reply);
    }

    @AfterEach
    void tearDown() {
        for (var extension : created.reversed()) {
            var name = ExtensionStoreUtil.buildStoreName(
                    schemeManager.get(extension.getClass()),
                    extension.getMetadata().getName());
            var stored = storeClient.fetchByName(name).block();
            if (stored != null) {
                storeClient.delete(name, stored.getVersion()).block();
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
        "anonymous,    true,  false, true,  false, 200",
        "anonymous,    false, false, true,  false, 404",
        "anonymous,    true,  true,  true,  false, 404",
        "anonymous,    true,  false, false, false, 404",
        "anonymous,    true,  false, true,  true,  404",
        "unrelated,    true,  false, false, true,  404",
        "reply-owner,  true,  false, false, true,  200",
        "reply-owner,  true,  true,  true,  false, 404",
        "thread-owner, true,  true,  false, true,  200",
        "thread-owner, true,  false, false, true,  404",
        "moderator,    false, true,  false, true,  200"
    })
    void replyDetailUsesRealAuthenticationAndBothVisibilityLevels(
            String username,
            boolean parentApproved,
            boolean parentHidden,
            boolean replyApproved,
            boolean replyHidden,
            int status) {
        comment.getSpec().setApproved(parentApproved);
        comment.getSpec().setHidden(parentHidden);
        comment = client.update(comment).block();
        reply.getSpec().setApproved(replyApproved);
        reply.getSpec().setHidden(replyHidden);
        reply = client.update(reply).block();
        var request = http.get().uri(REPLY_API + "permalink-reply");
        if (!"anonymous".equals(username)) {
            createUser(username);
            if ("moderator".equals(username)) {
                save(RoleBinding.create(username, "role-template-view-comments"));
            }
            request.headers(headers -> headers.setBasicAuth(username, "permalink-test"));
        }
        var response = request.exchange()
                .expectStatus()
                .isEqualTo(status)
                .expectHeader()
                .valueEquals("Cache-Control", "no-store, private")
                .expectHeader()
                .value("Vary", value -> assertThat(value).contains("Cookie", "Authorization"));
        if (status == 200) {
            response.expectBody()
                    .jsonPath("$.permalink")
                    .isEqualTo(REPLY_URL)
                    .jsonPath("$.metadata.name")
                    .isEqualTo("permalink-reply");
        }
    }

    @Test
    void rejectsWrongParentAndDeletedTargets() {
        http.get().uri(REPLY_API + "missing").exchange().expectStatus().isNotFound();
        reply.getSpec().setCommentName("another-comment");
        reply = client.update(reply).block();
        http.get().uri(REPLY_API + "permalink-reply").exchange().expectStatus().isNotFound();
        reply.getSpec().setCommentName("permalink-comment");
        reply = client.update(reply).block();
        comment = client.delete(comment).block();
        http.get().uri(REPLY_API + "permalink-reply").exchange().expectStatus().isNotFound();
    }

    @Test
    void returnsDetachedSanitizedReplyRegardlessOfListPage() {
        reply.getSpec().getOwner().setKind(Comment.CommentOwner.KIND_EMAIL);
        reply.getSpec().getOwner().setName("private@example.test");
        reply.getSpec().getOwner().setAnnotations(Map.of("private", "secret", "website", "https://example.test"));
        reply.getSpec().setIpAddress("192.0.2.1");
        reply = client.update(reply).block();
        var before = JsonUtils.objectToJson(reply);
        var earlier = JsonUtils.deepCopy(reply);
        earlier.setMetadata(metadata("earlier-reply"));
        earlier.getSpec().setPriority(-1);
        save(earlier);
        http.get()
                .uri(REPLY_API + "permalink-reply")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.permalink")
                .isEqualTo(REPLY_URL)
                .jsonPath("$.spec.owner.name")
                .isEqualTo("")
                .jsonPath("$.spec.ipAddress")
                .isEqualTo("")
                .jsonPath("$.spec.owner.annotations.private")
                .doesNotExist()
                .jsonPath("$.spec.owner.annotations.website")
                .isEqualTo("https://example.test")
                .jsonPath("$.stats.upvote")
                .isEqualTo(0);
        assertThat(JsonUtils.objectToJson(
                        client.get(Reply.class, "permalink-reply").block()))
                .isEqualTo(before);
        reply = client.delete(reply).block();
        http.get().uri(REPLY_API + "permalink-reply").exchange().expectStatus().isNotFound();
    }

    @Test
    void resolvesSubjectOncePerReplyPage() {
        var second = JsonUtils.deepCopy(reply);
        second.setMetadata(metadata("second-reply"));
        save(second);
        clearInvocations(permalinkService);
        http.get()
                .uri("/apis/api.halo.run/v1alpha1/comments/permalink-comment/reply")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items.length()")
                .isEqualTo(2)
                .jsonPath("$.items[0].permalink")
                .isNotEmpty()
                .jsonPath("$.items[1].permalink")
                .isNotEmpty();
        verify(permalinkService, times(1)).getSubjectUrl(Ref.of(post));

        createUser("moderator");
        save(RoleBinding.create("moderator", "role-template-view-comments"));
        clearInvocations(permalinkService);
        http.get()
                .uri("/apis/api.console.halo.run/v1alpha1/replies?commentName=permalink-comment")
                .headers(headers -> headers.setBasicAuth("moderator", "permalink-test"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items.length()")
                .isEqualTo(2);
        verify(permalinkService, times(1)).getSubjectUrl(Ref.of(post));
    }

    @Test
    void preservesConfiguredAbsolutePermalinks() {
        haloProperties.setUseAbsolutePermalink(true);
        try {
            post.getStatusOrDefault().setPermalink(postPermalinkPolicy.permalink(post));
            post = client.update(post).block();
            var expected =
                    "https://configured.example.test/archives/permalink-post" + "#halo-comment=permalink-comment";
            http.get()
                    .uri("/apis/api.halo.run/v1alpha1/comments/permalink-comment")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.permalink")
                    .isEqualTo(expected);
            http.get()
                    .uri(REPLY_API + "permalink-reply")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.permalink")
                    .isEqualTo(expected + "&reply=permalink-reply");
        } finally {
            haloProperties.setUseAbsolutePermalink(false);
        }
    }

    @Test
    void publicAndConsoleResponsesShareCurrentPermalinks() {
        var originalComment = JsonUtils.objectToJson(comment);
        var rootApi = "/apis/api.halo.run/v1alpha1/comments/permalink-comment";
        http.get()
                .uri(rootApi)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.permalink")
                .isEqualTo(COMMENT_URL);
        assertThat(publicQueryService
                        .list(null, 1, 10)
                        .block()
                        .getItems()
                        .getFirst()
                        .getPermalink())
                .isEqualTo(COMMENT_URL);
        http.get()
                .uri(
                        "/apis/api.halo.run/v1alpha1/comments?group=content.halo.run&version=v1alpha1&kind=Post&name=permalink-post&withReplies=true")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items[0].permalink")
                .isEqualTo(COMMENT_URL)
                .jsonPath("$.items[0].replies.items[0].permalink")
                .isEqualTo(REPLY_URL);
        createUser("moderator");
        save(RoleBinding.create("moderator", "role-template-view-comments"));
        http.get()
                .uri("/apis/api.console.halo.run/v1alpha1/comments")
                .headers(headers -> headers.setBasicAuth("moderator", "permalink-test"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items[0].permalink")
                .isEqualTo(COMMENT_URL);
        http.get()
                .uri("/apis/api.console.halo.run/v1alpha1/replies?commentName=permalink-comment")
                .headers(headers -> headers.setBasicAuth("moderator", "permalink-test"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items[0].permalink")
                .isEqualTo(REPLY_URL);

        assertThat(JsonUtils.objectToJson(
                        client.get(Comment.class, "permalink-comment").block()))
                .isEqualTo(originalComment);
        post.getStatusOrDefault().setPermalink("https://example.test/renamed");
        post = client.update(post).block();
        http.get()
                .uri(rootApi)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.permalink")
                .isEqualTo("https://example.test/renamed#halo-comment=permalink-comment");
        post.getStatusOrDefault().setPermalink(null);
        post = client.update(post).block();
        http.get()
                .uri(rootApi)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("permalink-comment")
                .jsonPath("$.permalink")
                .doesNotExist();
        comment.getSpec().setSubjectRef(Ref.of("missing-post", Post.GVK));
        comment = client.update(comment).block();
        http.get()
                .uri(REPLY_API + "permalink-reply")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("permalink-reply")
                .jsonPath("$.permalink")
                .doesNotExist();
    }

    private void createUser(String name) {
        var user = new User();
        user.setMetadata(metadata(name));
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName(name);
        user.getSpec().setPassword(passwordEncoder.encode("permalink-test"));
        save(user);
    }

    private static Metadata metadata(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }

    private static void configure(Comment.BaseCommentSpec spec, String ownerName) {
        spec.setRaw("Comment body");
        spec.setContent("<p>Comment body</p>");
        spec.setApproved(true);
        spec.setAllowNotification(true);
        spec.setHidden(false);
        spec.setPriority(0);
        spec.setTop(false);
        var owner = new Comment.CommentOwner();
        owner.setKind(User.KIND);
        owner.setName(ownerName);
        spec.setOwner(owner);
    }

    private <T extends Extension> T save(T extension) {
        var saved = client.create(extension).block();
        created.add(saved);
        return saved;
    }
}
