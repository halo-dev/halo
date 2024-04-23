package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexerFactory;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Integration tests for {@link CommentServiceImpl}.
 *
 * @author guqing
 * @since 2.15.0
 */
class CommentServiceImplIntegrationTest {

    @Nested
    @DirtiesContext
    @SpringBootTest
    class CommentRemoveTest {
        private final List<Comment> storedComments = createComments(350);

        @Autowired
        private SchemeManager schemeManager;

        @SpyBean
        private ReactiveExtensionClient reactiveClient;

        @Autowired
        private ReactiveExtensionStoreClient storeClient;

        @Autowired
        private IndexerFactory indexerFactory;

        @SpyBean
        private CommentServiceImpl commentService;

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

        @BeforeEach
        void setUp() {
            Flux.fromIterable(storedComments)
                .flatMap(post -> reactiveClient.create(post))
                .as(StepVerifier::create)
                .expectNextCount(storedComments.size())
                .verifyComplete();
        }

        @AfterEach
        void tearDown() {
            Flux.fromIterable(storedComments)
                .flatMap(this::deleteImmediately)
                .as(StepVerifier::create)
                .expectNextCount(storedComments.size())
                .verifyComplete();
        }

        @Test
        void commentBatchDeletionTest() {
            Ref ref = Ref.of("67",
                GroupVersionKind.fromAPIVersionAndKind("content.halo.run/v1alpha1", "SinglePage"));
            commentService.removeBySubject(ref)
                .as(StepVerifier::create)
                .verifyComplete();

            verify(reactiveClient, times(storedComments.size())).delete(any(Comment.class));
            verify(commentService, times(2)).listCommentsByRef(eq(ref), any());

            commentService.listCommentsByRef(ref, PageRequestImpl.ofSize(1))
                .as(StepVerifier::create)
                .consumeNextWith(result -> {
                    assertThat(result.getTotal()).isEqualTo(0);
                })
                .verifyComplete();
        }

        List<Comment> createComments(int size) {
            List<Comment> comments = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                var comment = createComment();
                comment.getMetadata().setName("comment-" + i);
                comments.add(comment);
            }
            return comments;
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
                    "generateName": "comment-"
                }
            }
            """, Comment.class);
    }
}