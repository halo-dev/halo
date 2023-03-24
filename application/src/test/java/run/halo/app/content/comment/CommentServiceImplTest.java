package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.plugin.ExtensionComponentsFinder;

/**
 * Tests for {@link CommentServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
class CommentServiceImplTest {

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private UserService userService;

    @Mock
    private ExtensionComponentsFinder extensionComponentsFinder;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CounterService counterService;

    @BeforeEach
    void setUp() {
        SystemSetting.Comment commentSetting = getCommentSetting();
        lenient().when(environmentFetcher.fetchComment()).thenReturn(Mono.just(commentSetting));

        ListResult<Comment> comments = new ListResult<>(1, 10, 3, comments());
        when(client.list(eq(Comment.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(comments));

        when(userService.getUserOrGhost(eq("A-owner")))
                .thenReturn(Mono.just(createUser("A-owner")));
        when(userService.getUserOrGhost(eq("B-owner")))
                .thenReturn(Mono.just(createUser("B-owner")));
        when(client.fetch(eq(User.class), eq("C-owner")))
            .thenReturn(Mono.empty());

        PostCommentSubject postCommentSubject = Mockito.mock(PostCommentSubject.class);
        when(extensionComponentsFinder.getExtensions(eq(CommentSubject.class)))
            .thenReturn(List.of(postCommentSubject));

        when(postCommentSubject.supports(any())).thenReturn(true);
        when(postCommentSubject.get(eq("fake-post"))).thenReturn(Mono.just(post()));
    }

    private static User createUser(String name) {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        user.setSpec(new User.UserSpec());
        user.getSpec().setAvatar(name + "-avatar");
        user.getSpec().setDisplayName(name + "-displayName");
        user.getSpec().setEmail(name + "-email");
        return user;
    }

    @Test
    void listComment() {
        when(userService.getUserOrGhost(any()))
            .thenReturn(Mono.just(ghostUser()));
        when(userService.getUserOrGhost("A-owner"))
            .thenReturn(Mono.just(createUser("A-owner")));
        when(userService.getUserOrGhost("B-owner"))
            .thenReturn(Mono.just(createUser("B-owner")));

        Mono<ListResult<ListedComment>> listResultMono =
            commentService.listComment(new CommentQuery(new LinkedMultiValueMap<>()));
        Counter counterA = new Counter();
        counterA.setUpvote(3);
        String commentACounter = MeterUtils.nameOf(Comment.class, "A");
        when(counterService.getByName(eq(commentACounter))).thenReturn(Mono.just(counterA));

        Counter counterB = new Counter();
        counterB.setUpvote(9);
        String commentBCounter = MeterUtils.nameOf(Comment.class, "B");
        when(counterService.getByName(eq(commentBCounter))).thenReturn(Mono.just(counterB));

        Counter counterC = new Counter();
        counterC.setUpvote(0);
        String commentCCounter = MeterUtils.nameOf(Comment.class, "C");
        when(counterService.getByName(eq(commentCCounter))).thenReturn(Mono.just(counterC));

        StepVerifier.create(listResultMono)
            .consumeNextWith(result -> {
                try {
                    JSONAssert.assertEquals(expectListResultJson(),
                        JsonUtils.objectToJson(result),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    @WithMockUser(username = "B-owner")
    void create() throws JSONException {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setRaw("fake-raw");
        commentRequest.setContent("fake-content");
        commentRequest.setAllowNotification(true);
        commentRequest.setSubjectRef(Ref.of(post()));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        when(client.fetch(eq(User.class), eq("B-owner")))
                .thenReturn(Mono.just(createUser("B-owner")));
        Comment commentToCreate = commentRequest.toComment();
        commentToCreate.getMetadata().setName("fake");
        Mono<Comment> commentMono = commentService.create(commentToCreate);
        when(client.create(any())).thenReturn(Mono.empty());
        StepVerifier.create(commentMono)
            .verifyComplete();

        verify(client, times(1)).create(captor.capture());
        Comment comment = captor.getValue();
        comment.getSpec().setCreationTime(null);
        JSONAssert.assertEquals("""
                {
                    "spec": {
                        "raw": "fake-raw",
                        "content": "fake-content",
                        "owner": {
                            "kind": "User",
                            "name": "B-owner",
                            "displayName": "B-owner-displayName"
                        },
                        "priority": 0,
                        "top": false,
                        "allowNotification": true,
                        "approved": false,
                        "hidden": false,
                        "subjectRef": {
                            "group": "content.halo.run",
                            "version": "v1alpha1",
                            "kind": "Post",
                            "name": "fake-post"
                        }
                    },
                    "apiVersion": "content.halo.run/v1alpha1",
                    "kind": "Comment",
                    "metadata": {
                        "name": "fake"
                    }
                }
                """,
            JsonUtils.objectToJson(comment),
            true);
    }

    @Test
    void commentPredicate() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("keyword", "hello");
        queryParams.add("approved", "true");
        queryParams.add("hidden", "false");
        queryParams.add("allowNotification", "true");
        queryParams.add("top", "false");
        queryParams.add("ownerKind", "User");
        queryParams.add("ownerName", "fake-user");
        queryParams.add("subjectKind", "Post");
        queryParams.add("subjectName", "fake-post");

        final Predicate<Comment> predicate =
            commentService.commentPredicate(new CommentQuery(queryParams));

        Comment comment = comment("A");
        comment.getSpec().setRaw("hello-world");
        comment.getSpec().setApproved(true);
        comment.getSpec().setHidden(false);
        comment.getSpec().setAllowNotification(true);
        comment.getSpec().setTop(false);
        Comment.CommentOwner commentOwner = new Comment.CommentOwner();
        commentOwner.setKind("User");
        commentOwner.setName("fake-user");
        commentOwner.setDisplayName("fake-user-display-name");
        comment.getSpec().setOwner(commentOwner);
        comment.getSpec().setSubjectRef(Ref.of(post()));
        assertThat(predicate.test(comment)).isTrue();

        queryParams.remove("keyword");
        queryParams.add("keyword", "nothing");
        final Predicate<Comment> predicateTwo =
            commentService.commentPredicate(new CommentQuery(queryParams));
        assertThat(predicateTwo.test(comment)).isFalse();
    }

    private List<Comment> comments() {
        Comment a = comment("A");
        a.getSpec().getOwner().setKind(Comment.CommentOwner.KIND_EMAIL);
        a.getSpec().getOwner()
            .setAnnotations(Map.of(Comment.CommentOwner.AVATAR_ANNO, "avatar",
                Comment.CommentOwner.WEBSITE_ANNO, "website"));
        return List.of(a, comment("B"), comment("C"));
    }

    private Comment comment(String name) {
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName(name);

        comment.setSpec(new Comment.CommentSpec());
        Comment.CommentOwner commentOwner = new Comment.CommentOwner();
        commentOwner.setKind(User.KIND);
        commentOwner.setDisplayName("displayName");
        commentOwner.setName(name + "-owner");
        comment.getSpec().setOwner(commentOwner);

        comment.getSpec().setSubjectRef(Ref.of(post()));

        comment.setStatus(new Comment.CommentStatus());
        return comment;
    }

    private Post post() {
        Post post = TestPost.postV1();
        post.getMetadata().setName("fake-post");
        return post;
    }

    private static SystemSetting.Comment getCommentSetting() {
        SystemSetting.Comment commentSetting = new SystemSetting.Comment();
        commentSetting.setEnable(true);
        commentSetting.setSystemUserOnly(true);
        commentSetting.setRequireReviewForNew(true);
        return commentSetting;
    }

    User ghostUser() {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("ghost");
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName("Ghost");
        user.getSpec().setEmail("");
        return user;
    }

    private String expectListResultJson() {
        return """
            {
                "page": 1,
                "size": 10,
                "total": 3,
                "totalPages": 1,
                "items": [
                    {
                        "comment": {
                            "spec": {
                                "owner": {
                                    "kind": "Email",
                                    "name": "A-owner",
                                    "displayName": "displayName",
                                    "annotations": {
                                        "website": "website",
                                        "avatar": "avatar"
                                    }
                                },
                                "subjectRef": {
                                    "group": "content.halo.run",
                                    "version": "v1alpha1",
                                    "kind": "Post",
                                    "name": "fake-post"
                                }
                            },
                            "status": {},
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Comment",
                            "metadata": {
                                "name": "A"
                            }
                        },
                        "owner": {
                            "kind": "Email",
                            "name": "A-owner",
                            "displayName": "displayName",
                            "avatar": "avatar",
                            "email": "A-owner"
                        },
                        "subject": {
                            "spec": {
                                "title": "post-A",
                                "headSnapshot": "base-snapshot",
                                "baseSnapshot": "snapshot-A"
                            },
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Post",
                            "metadata": {
                                "name": "fake-post"
                            }
                        },
                        "stats": {
                            "upvote": 3
                        }
                    },
                    {
                        "comment": {
                            "spec": {
                                "owner": {
                                    "kind": "User",
                                    "name": "B-owner",
                                    "displayName": "displayName"
                                },
                                "subjectRef": {
                                    "group": "content.halo.run",
                                    "version": "v1alpha1",
                                    "kind": "Post",
                                    "name": "fake-post"
                                }
                            },
                            "status": {},
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Comment",
                            "metadata": {
                                "name": "B"
                            }
                        },
                        "owner": {
                            "kind": "User",
                            "name": "B-owner",
                            "displayName": "B-owner-displayName",
                            "avatar": "B-owner-avatar",
                            "email": "B-owner-email"
                        },
                        "subject": {
                            "spec": {
                                "title": "post-A",
                                "headSnapshot": "base-snapshot",
                                "baseSnapshot": "snapshot-A"
                            },
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Post",
                            "metadata": {
                                "name": "fake-post"
                            }
                        },
                        "stats": {
                           "upvote": 9
                        }
                    },
                    {
                        "comment": {
                            "spec": {
                                "owner": {
                                    "kind": "User",
                                    "name": "C-owner",
                                    "displayName": "displayName"
                                },
                                "subjectRef": {
                                    "group": "content.halo.run",
                                    "version": "v1alpha1",
                                    "kind": "Post",
                                    "name": "fake-post"
                                }
                            },
                            "status": {},
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Comment",
                            "metadata": {
                                "name": "C"
                            }
                        },
                        "owner": {
                            "kind": "User",
                            "name": "ghost",
                            "displayName": "Ghost",
                            "email": ""
                        },
                        "subject": {
                            "spec": {
                                "title": "post-A",
                                "headSnapshot": "base-snapshot",
                                "baseSnapshot": "snapshot-A"
                            },
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Post",
                            "metadata": {
                                "name": "fake-post"
                            }
                        },
                        "stats": {
                            "upvote": 0
                        }
                    }
                ],
                "first": true,
                "last": true,
                "hasNext": false,
                "hasPrevious": false
            }
            """;
    }
}