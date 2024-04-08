package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexerFactory;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.utils.JsonUtils;

@DirtiesContext
@SpringBootTest
class CommentPublicQueryServiceIntegrationTest {

    @Autowired
    private SchemeManager schemeManager;

    @Autowired
    private ReactiveExtensionClient client;

    @Autowired
    private ReactiveExtensionStoreClient storeClient;

    @Autowired
    private IndexerFactory indexerFactory;

    Mono<Extension> deleteImmediately(Extension extension) {
        var name = extension.getMetadata().getName();
        var scheme = schemeManager.get(extension.getClass());
        // un-index
        var indexer = indexerFactory.getIndexer(extension.groupVersionKind());
        indexer.unIndexRecord(extension.getMetadata().getName());

        // delete from db
        var storeName = ExtensionStoreUtil.buildStoreName(scheme, name);
        return storeClient.delete(storeName, extension.getMetadata().getVersion())
            .thenReturn(extension);
    }

    @Nested
    class CommentListTest {
        private final List<Comment> storedComments = commentsForStore();

        @Autowired
        private CommentPublicQueryServiceImpl commentPublicQueryService;

        @BeforeEach
        void setUp() {
            Flux.fromIterable(storedComments)
                .flatMap(comment -> client.create(comment))
                .as(StepVerifier::create)
                .expectNextCount(storedComments.size())
                .verifyComplete();
        }

        @AfterEach
        void tearDown() {
            Flux.fromIterable(storedComments)
                .flatMap(CommentPublicQueryServiceIntegrationTest.this::deleteImmediately)
                .as(StepVerifier::create)
                .expectNextCount(storedComments.size())
                .verifyComplete();
        }

        @Test
        void listWhenUserNotLogin() {
            Ref ref = Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class));
            commentPublicQueryService.list(ref, 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("comment-approved");
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = AnonymousUserConst.PRINCIPAL)
        void listWhenUserIsAnonymous() {
            Ref ref = Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class));
            commentPublicQueryService.list(ref, 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("comment-approved");
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = "fake-user")
        void listWhenUserLoggedIn() {
            Ref ref = Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class));
            commentPublicQueryService.list(ref, 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(3);
                    assertThat(listResult.getItems().size()).isEqualTo(3);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("comment-approved");
                    assertThat(listResult.getItems().get(1).getMetadata().getName())
                        .isEqualTo("comment-approved-but-another-owner");
                    assertThat(listResult.getItems().get(2).getMetadata().getName())
                        .isEqualTo("comment-not-approved");
                })
                .verifyComplete();
        }

        List<Comment> commentsForStore() {
            // Mock
            Comment commentNotApproved = fakeComment();
            commentNotApproved.getMetadata().setName("comment-not-approved");
            commentNotApproved.getSpec().setApproved(false);

            Comment commentApproved = fakeComment();
            commentApproved.getMetadata().setName("comment-approved");
            commentApproved.getSpec().setApproved(true);

            Comment notApprovedWithAnonymous = fakeComment();
            notApprovedWithAnonymous.getMetadata().setName("comment-not-approved-anonymous");
            notApprovedWithAnonymous.getSpec().setApproved(false);
            notApprovedWithAnonymous.getSpec().getOwner().setName(AnonymousUserConst.PRINCIPAL);

            Comment commentApprovedButAnotherOwner = fakeComment();
            commentApprovedButAnotherOwner.getMetadata()
                .setName("comment-approved-but-another-owner");
            commentApprovedButAnotherOwner.getSpec().setApproved(true);
            commentApprovedButAnotherOwner.getSpec().getOwner().setName("another");

            Comment commentNotApprovedAndAnotherOwner = fakeComment();
            commentNotApprovedAndAnotherOwner.getMetadata()
                .setName("comment-not-approved-and-another");
            commentNotApprovedAndAnotherOwner.getSpec().setApproved(false);
            commentNotApprovedAndAnotherOwner.getSpec().getOwner().setName("another");

            Comment notApprovedAndAnotherRef = fakeComment();
            notApprovedAndAnotherRef.getMetadata()
                .setName("comment-not-approved-and-another-ref");
            notApprovedAndAnotherRef.getSpec().setApproved(false);
            Ref anotherRef =
                Ref.of("another-fake-post", GroupVersionKind.fromExtension(Post.class));
            notApprovedAndAnotherRef.getSpec().setSubjectRef(anotherRef);

            return List.of(
                commentNotApproved,
                commentApproved,
                commentApprovedButAnotherOwner,
                commentNotApprovedAndAnotherOwner,
                notApprovedWithAnonymous,
                notApprovedAndAnotherRef
            );
        }

        Comment fakeComment() {
            Comment comment = createComment();
            comment.getMetadata().setDeletionTimestamp(null);
            comment.getMetadata().setName("fake-comment");

            comment.getSpec().setRaw("fake-raw");
            comment.getSpec().setContent("fake-content");
            comment.getSpec().setHidden(false);
            comment.getSpec()
                .setSubjectRef(Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class)));
            Comment.CommentOwner commentOwner = new Comment.CommentOwner();
            commentOwner.setKind(User.KIND);
            commentOwner.setName("fake-user");
            commentOwner.setDisplayName("fake-display-name");
            comment.getSpec().setOwner(commentOwner);
            return comment;
        }
    }

    @Nested
    class CommentDefaultSortTest {
        private final List<Comment> commentList = createCommentList();

        @BeforeEach
        void setUp() {
            Flux.fromIterable(commentList)
                .flatMap(comment -> client.create(comment))
                .as(StepVerifier::create)
                .expectNextCount(commentList.size())
                .verifyComplete();
        }

        @AfterEach
        void tearDown() {
            Flux.fromIterable(commentList)
                .flatMap(CommentPublicQueryServiceIntegrationTest.this::deleteImmediately)
                .as(StepVerifier::create)
                .expectNextCount(commentList.size())
                .verifyComplete();
        }

        @Test
        void sortTest() {
            var comments =
                client.listAll(Comment.class, new ListOptions(),
                        CommentPublicQueryServiceImpl.defaultCommentSort())
                    .collectList()
                    .block();
            assertThat(comments).isNotNull();

            var result = comments.stream()
                .map(comment -> comment.getMetadata().getName())
                .collect(Collectors.joining(", "));
            assertThat(result).isEqualTo("1, 2, 4, 3, 5, 6, 9, 10, 14, 8, 7, 11, 12, 13");
        }

        List<Comment> createCommentList() {
            // 1, now + 1s, top, 0
            // 2, now + 2s, top, 1
            // 3, now + 3s, top, 2
            // 4, now + 4s, top, 2
            // 5, now + 4s, top, 3
            // 6, now + 1s, no, 0
            // 7, now + 2s, no, 0
            // 8, now + 3s, no, 0
            // 9, now + 3s, no, 0
            // 10, null, no, 0
            // 11, null, no, 1
            // 12, null, no, 3
            // 13, now + 3s, no, 3
            Instant now = Instant.now();
            var comment1 = commentForCompare("1", now.plusSeconds(1), true, 0);
            var comment2 = commentForCompare("2", now.plusSeconds(2), true, 1);
            var comment3 = commentForCompare("3", now.plusSeconds(3), true, 2);
            var comment4 = commentForCompare("4", now.plusSeconds(4), true, 2);
            var comment5 = commentForCompare("5", now.plusSeconds(4), true, 3);
            var comment6 = commentForCompare("6", now.plusSeconds(4), true, 3);
            var comment7 = commentForCompare("7", now.plusSeconds(1), false, 0);
            var comment8 = commentForCompare("8", now.plusSeconds(2), false, 0);
            var comment9 = commentForCompare("9", now.plusSeconds(3), false, 0);
            var comment10 = commentForCompare("10", now.plusSeconds(3), false, 0);
            var comment11 = commentForCompare("11", now, false, 0);
            var comment12 = commentForCompare("12", now, false, 1);
            var comment13 = commentForCompare("13", now, false, 3);
            var comment14 = commentForCompare("14", now.plusSeconds(3), false, 3);

            return List.of(comment1, comment2, comment3, comment4, comment5, comment6, comment7,
                comment8, comment9, comment10, comment11, comment12, comment13, comment14);
        }

        Comment commentForCompare(String name, Instant creationTime, boolean top, int priority) {
            var comment = createComment();
            comment.getMetadata().setName(name);
            comment.getMetadata().setCreationTimestamp(creationTime);
            comment.getSpec().setCreationTime(creationTime);
            comment.getSpec().setTop(top);
            comment.getSpec().setPriority(priority);
            return comment;
        }
    }

    @Nested
    class ListReplyTest {
        private final List<Reply> storedReplies = mockRelies();
        @Autowired
        private CommentPublicQueryServiceImpl commentPublicQueryService;

        @BeforeEach
        void setUp() {
            Flux.fromIterable(storedReplies)
                .flatMap(reply -> client.create(reply))
                .as(StepVerifier::create)
                .expectNextCount(storedReplies.size())
                .verifyComplete();
        }

        @AfterEach
        void tearDown() {
            Flux.fromIterable(storedReplies)
                .flatMap(CommentPublicQueryServiceIntegrationTest.this::deleteImmediately)
                .as(StepVerifier::create)
                .expectNextCount(storedReplies.size())
                .verifyComplete();
        }

        @Test
        void listWhenUserNotLogin() {
            commentPublicQueryService.listReply("fake-comment", 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("reply-approved");
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = AnonymousUserConst.PRINCIPAL)
        void listWhenUserIsAnonymous() {
            commentPublicQueryService.listReply("fake-comment", 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("reply-approved");
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = "fake-user")
        void listWhenUserLoggedIn() {
            commentPublicQueryService.listReply("fake-comment", 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(3);
                    assertThat(listResult.getItems().size()).isEqualTo(3);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("reply-approved");
                    assertThat(listResult.getItems().get(1).getMetadata().getName())
                        .isEqualTo("reply-approved-but-another-owner");
                    assertThat(listResult.getItems().get(2).getMetadata().getName())
                        .isEqualTo("reply-not-approved");
                })
                .verifyComplete();
        }

        @Test
        void desensitizeReply() throws JSONException {
            var reply = createReply();
            reply.getSpec().getOwner()
                .setAnnotations(new HashMap<>() {
                    {
                        put(Comment.CommentOwner.KIND_EMAIL, "mail@halo.run");
                    }
                });
            reply.getSpec().setIpAddress("127.0.0.1");

            var result = commentPublicQueryService.toReplyVo(reply).block();
            result.getMetadata().setCreationTimestamp(null);
            var jsonObject = JsonUtils.jsonToObject(fakeReplyJson(), JsonNode.class);
            ((ObjectNode) jsonObject.get("owner"))
                .put("displayName", "已删除用户");
            JSONAssert.assertEquals(jsonObject.toString(),
                JsonUtils.objectToJson(result),
                true);
        }

        String fakeReplyJson() {
            return """
                    {
                        "metadata":{
                            "name":"fake-reply"
                        },
                        "spec":{
                            "raw":"fake-raw",
                            "content":"fake-content",
                            "owner":{
                                "kind":"User",
                                "name":"",
                                "displayName":"fake-display-name",
                                "annotations":{
                                    "email-hash": "4249f4df72b475e7894fabed1c5888cf"
                                }
                            },
                            "creationTime": "2024-03-11T06:23:42.923294424Z",
                            "ipAddress":"",
                            "hidden": false,
                            "allowNotification": false,
                            "top": false,
                            "priority": 0,
                            "commentName":"fake-comment"
                        },
                        "owner":{
                            "kind":"User",
                            "displayName":"fake-display-name"
                        },
                        "stats":{
                            "upvote":0
                        }
                    }
                """;
        }

        private List<Reply> mockRelies() {
            // Mock
            Reply notApproved = createReply();
            notApproved.getMetadata().setName("reply-not-approved");
            notApproved.getSpec().setApproved(false);

            Reply approved = createReply();
            approved.getMetadata().setName("reply-approved");
            approved.getSpec().setApproved(true);

            Reply notApprovedWithAnonymous = createReply();
            notApprovedWithAnonymous.getMetadata().setName("reply-not-approved-anonymous");
            notApprovedWithAnonymous.getSpec().setApproved(false);
            notApprovedWithAnonymous.getSpec().getOwner().setName(AnonymousUserConst.PRINCIPAL);

            Reply approvedButAnotherOwner = createReply();
            approvedButAnotherOwner.getMetadata()
                .setName("reply-approved-but-another-owner");
            approvedButAnotherOwner.getSpec().setApproved(true);
            approvedButAnotherOwner.getSpec().getOwner().setName("another");

            Reply notApprovedAndAnotherOwner = createReply();
            notApprovedAndAnotherOwner.getMetadata()
                .setName("reply-not-approved-and-another");
            notApprovedAndAnotherOwner.getSpec().setApproved(false);
            notApprovedAndAnotherOwner.getSpec().getOwner().setName("another");

            Reply notApprovedAndAnotherCommentName = createReply();
            notApprovedAndAnotherCommentName.getMetadata()
                .setName("reply-approved-and-another-comment-name");
            notApprovedAndAnotherCommentName.getSpec().setApproved(false);
            notApprovedAndAnotherCommentName.getSpec().setCommentName("another-fake-comment");

            return List.of(
                notApproved,
                approved,
                approvedButAnotherOwner,
                notApprovedAndAnotherOwner,
                notApprovedWithAnonymous,
                notApprovedAndAnotherCommentName
            );
        }

        Reply createReply() {
            var reply = JsonUtils.jsonToObject(fakeReplyJson(), Reply.class);
            reply.getMetadata().setName("fake-reply");

            reply.getSpec().setRaw("fake-raw");
            reply.getSpec().setContent("fake-content");
            reply.getSpec().setHidden(false);
            reply.getSpec().setCommentName("fake-comment");
            Comment.CommentOwner commentOwner = new Comment.CommentOwner();
            commentOwner.setKind(User.KIND);
            commentOwner.setName("fake-user");
            commentOwner.setDisplayName("fake-display-name");
            reply.getSpec().setOwner(commentOwner);
            return reply;
        }
    }

    Comment createComment() {
        return JsonUtils.jsonToObject("""
              {
                "spec": {
                    "raw": "fake-raw",
                    "content": "fake-content",
                    "owner": {
                        "kind": "User",
                        "name": "fake-user"
                    },
                    "userAgent": "",
                    "ipAddress": "",
                    "approvedTime": "2024-02-28T09:15:16.095Z",
                    "creationTime": "2024-02-28T06:23:42.923294424Z",
                    "priority": 0,
                    "top": false,
                    "allowNotification": false,
                    "approved": true,
                    "hidden": false,
                    "subjectRef": {
                        "group": "content.halo.run",
                        "version": "v1alpha1",
                        "kind": "SinglePage",
                        "name": "67"
                    },
                    "lastReadTime": "2024-02-29T03:39:04.230Z"
                },
                "apiVersion": "content.halo.run/v1alpha1",
                "kind": "Comment",
                "metadata": {
                    "name": "fake-comment",
                    "creationTimestamp": "2024-02-28T06:23:42.923439037Z"
                }
            }
            """, Comment.class);
    }
}
