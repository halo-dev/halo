package run.halo.app.theme.finders.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.utils.JsonUtils;
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
        void desensitizeComment() throws JSONException {
            var commentOwner = new Comment.CommentOwner();
            commentOwner.setName("fake-user");
            commentOwner.setDisplayName("Fake User");
            commentOwner.setAnnotations(new HashMap<>() {
                {
                    put(Comment.CommentOwner.KIND_EMAIL, "mail@halo.run");
                }
            });
            var comment = commentForCompare("1", null, true, 0);
            comment.getSpec().setIpAddress("127.0.0.1");
            comment.getSpec().setOwner(commentOwner);

            Counter counter = new Counter();
            counter.setUpvote(0);
            when(counterService.getByName(any())).thenReturn(Mono.just(counter));

            var result = commentPublicQueryService.toCommentVo(comment).block();
            result.getMetadata().setCreationTimestamp(null);
            result.getSpec().setCreationTime(null);
            JSONAssert.assertEquals("""
                    {
                         "metadata":{
                             "name":"1"
                         },
                         "spec":{
                             "owner":{
                                 "name":"",
                                 "displayName":"Fake User",
                                 "annotations":{
                     
                                 }
                             },
                             "ipAddress":"",
                             "priority":0,
                             "top":true
                         },
                         "owner":{
                             "kind":"User",
                             "displayName":"fake-display-name"
                         },
                         "stats":{
                             "upvote":0
                         }
                     }
                    """,
                JsonUtils.objectToJson(result),
                true);
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