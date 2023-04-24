package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.metrics.CounterService;

/**
 * Tests for {@link CommentFinderImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
class CommentPublicQueryServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;
    @Mock
    private UserService userService;

    @Mock
    private CounterService counterService;

    @InjectMocks
    private CommentPublicQueryServiceImpl commentPublicQueryService;

    @BeforeEach
    void setUp() {
        User ghost = createUser();
        ghost.getMetadata().setName("ghost");
        when(userService.getUserOrGhost(eq("ghost"))).thenReturn(Mono.just(ghost));
        when(userService.getUserOrGhost(eq("fake-user"))).thenReturn(Mono.just(createUser()));
    }

    @Nested
    class ListCommentTest {
        @Test
        void listWhenUserNotLogin() {
            // Mock
            mockWhenListComment();

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
            // Mock
            mockWhenListComment();

            Ref ref = Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class));
            commentPublicQueryService.list(ref, 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("comment-approved");
                    assertThat(listResult.getItems().get(0).getStats().getUpvote()).isEqualTo(9);
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = "fake-user")
        void listWhenUserLoggedIn() {
            mockWhenListComment();

            Ref ref = Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class));
            commentPublicQueryService.list(ref, 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(3);
                    assertThat(listResult.getItems().size()).isEqualTo(3);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("comment-not-approved");
                    assertThat(listResult.getItems().get(1).getMetadata().getName())
                        .isEqualTo("comment-approved");
                })
                .verifyComplete();
        }


        @Test
        void commentComparator() {
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
            var comment11 = commentForCompare("11", null, false, 0);
            var comment12 = commentForCompare("12", null, false, 1);
            var comment13 = commentForCompare("13", null, false, 3);
            var comment14 = commentForCompare("14", now.plusSeconds(3), false, 3);

            var result = Stream.of(comment1, comment2, comment3, comment4, comment5, comment6,
                    comment7, comment8, comment9, comment10, comment11, comment12, comment13,
                    comment14)
                .sorted(CommentPublicQueryServiceImpl.defaultComparator())
                .map(Comment::getMetadata)
                .map(MetadataOperator::getName)
                .collect(Collectors.joining(", "));
            assertThat(result).isEqualTo("1, 2, 3, 4, 5, 6, 9, 14, 10, 8, 7, 13, 12, 11");
        }

        Comment commentForCompare(String name, Instant creationTime, boolean top, int priority) {
            Comment comment = new Comment();
            comment.setMetadata(new Metadata());
            comment.getMetadata().setName(name);
            comment.getMetadata().setCreationTimestamp(Instant.now());
            comment.setSpec(new Comment.CommentSpec());
            comment.getSpec().setCreationTime(creationTime);
            comment.getSpec().setTop(top);
            comment.getSpec().setPriority(priority);
            return comment;
        }

        @SuppressWarnings("unchecked")
        private void mockWhenListComment() {
            // Mock
            Comment commentNotApproved = createComment();
            commentNotApproved.getMetadata().setName("comment-not-approved");
            commentNotApproved.getSpec().setApproved(false);

            Comment commentApproved = createComment();
            commentApproved.getMetadata().setName("comment-approved");
            commentApproved.getSpec().setApproved(true);

            Comment notApprovedWithAnonymous = createComment();
            notApprovedWithAnonymous.getMetadata().setName("comment-not-approved-anonymous");
            notApprovedWithAnonymous.getSpec().setApproved(false);
            notApprovedWithAnonymous.getSpec().getOwner().setName(AnonymousUserConst.PRINCIPAL);

            Comment commentApprovedButAnotherOwner = createComment();
            commentApprovedButAnotherOwner.getMetadata()
                .setName("comment-approved-but-another-owner");
            commentApprovedButAnotherOwner.getSpec().setApproved(true);
            commentApprovedButAnotherOwner.getSpec().getOwner().setName("another");

            Comment commentNotApprovedAndAnotherOwner = createComment();
            commentNotApprovedAndAnotherOwner.getMetadata()
                .setName("comment-not-approved-and-another");
            commentNotApprovedAndAnotherOwner.getSpec().setApproved(false);
            commentNotApprovedAndAnotherOwner.getSpec().getOwner().setName("another");

            Comment notApprovedAndAnotherRef = createComment();
            notApprovedAndAnotherRef.getMetadata()
                .setName("comment-not-approved-and-another-ref");
            notApprovedAndAnotherRef.getSpec().setApproved(false);
            Ref anotherRef =
                Ref.of("another-fake-post", GroupVersionKind.fromExtension(Post.class));
            notApprovedAndAnotherRef.getSpec().setSubjectRef(anotherRef);

            when(client.list(eq(Comment.class), any(),
                any(),
                eq(1),
                eq(10))
            ).thenAnswer((Answer<Mono<ListResult<Comment>>>) invocation -> {
                Predicate<Comment> predicate =
                    invocation.getArgument(1, Predicate.class);
                List<Comment> comments = Stream.of(
                    commentNotApproved,
                    commentApproved,
                    commentApprovedButAnotherOwner,
                    commentNotApprovedAndAnotherOwner,
                    notApprovedWithAnonymous,
                    notApprovedAndAnotherRef
                ).filter(predicate).toList();
                return Mono.just(new ListResult<>(1, 10, comments.size(), comments));
            });

            extractedUser();
            when(client.fetch(eq(User.class), any())).thenReturn(Mono.just(createUser()));

            Counter counter = new Counter();
            counter.setUpvote(9);
            when(counterService.getByName(any())).thenReturn(Mono.just(counter));
        }

        Comment createComment() {
            Comment comment = new Comment();
            comment.setMetadata(new Metadata());
            comment.getMetadata().setName("fake-comment");
            comment.setSpec(new Comment.CommentSpec());
            comment.setStatus(new Comment.CommentStatus());

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
    class ListReplyTest {
        @Test
        void listWhenUserNotLogin() {
            // Mock
            mockWhenListRely();

            commentPublicQueryService.listReply("fake-comment", 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(2);
                    assertThat(listResult.getItems().size()).isEqualTo(2);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("reply-approved");
                    assertThat(listResult.getItems().get(0).getStats().getUpvote()).isEqualTo(9);
                })
                .verifyComplete();
        }

        @Test
        @WithMockUser(username = AnonymousUserConst.PRINCIPAL)
        void listWhenUserIsAnonymous() {
            // Mock
            mockWhenListRely();

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
            mockWhenListRely();

            commentPublicQueryService.listReply("fake-comment", 1, 10)
                .as(StepVerifier::create)
                .consumeNextWith(listResult -> {
                    assertThat(listResult.getTotal()).isEqualTo(3);
                    assertThat(listResult.getItems().size()).isEqualTo(3);
                    assertThat(listResult.getItems().get(0).getMetadata().getName())
                        .isEqualTo("reply-not-approved");
                    assertThat(listResult.getItems().get(1).getMetadata().getName())
                        .isEqualTo("reply-approved");
                })
                .verifyComplete();
        }

        @SuppressWarnings("unchecked")
        private void mockWhenListRely() {
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

            when(client.list(eq(Reply.class), any(),
                any(),
                eq(1),
                eq(10))
            ).thenAnswer((Answer<Mono<ListResult<Reply>>>) invocation -> {
                Predicate<Reply> predicate =
                    invocation.getArgument(1, Predicate.class);
                List<Reply> replies = Stream.of(
                    notApproved,
                    approved,
                    approvedButAnotherOwner,
                    notApprovedAndAnotherOwner,
                    notApprovedWithAnonymous,
                    notApprovedAndAnotherCommentName
                ).filter(predicate).toList();
                return Mono.just(new ListResult<>(1, 10, replies.size(), replies));
            });

            extractedUser();
            when(client.fetch(eq(User.class), any())).thenReturn(Mono.just(createUser()));

            Counter counter = new Counter();
            counter.setUpvote(9);
            when(counterService.getByName(any())).thenReturn(Mono.just(counter));
        }

        Reply createReply() {
            Reply reply = new Reply();
            reply.setMetadata(new Metadata());
            reply.getMetadata().setName("fake-reply");
            reply.setSpec(new Reply.ReplySpec());

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

    private void extractedUser() {
        User another = createUser();
        another.getMetadata().setName("another");
        when(userService.getUserOrGhost(eq("another"))).thenReturn(Mono.just(another));

        User ghost = createUser();
        ghost.getMetadata().setName("ghost");
        when(userService.getUserOrGhost(eq("ghost"))).thenReturn(Mono.just(ghost));
        when(userService.getUserOrGhost(eq("fake-user"))).thenReturn(Mono.just(createUser()));
        when(userService.getUserOrGhost(any())).thenReturn(Mono.just(ghost));
    }

    User createUser() {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-user");
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName("fake-display-name");
        return user;
    }
}