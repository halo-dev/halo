package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link CommentRequest}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CommentRequestTest {

    @Test
    void constructor() throws JSONException {
        CommentRequest commentRequest = createCommentRequest();

        JSONAssert.assertEquals("""
                {
                    "subjectRef": {
                        "group": "fake.halo.run",
                        "version": "v1alpha1",
                        "kind": "Fake",
                        "name": "fake"
                    },
                    "raw": "raw",
                    "content": "content",
                    "allowNotification": true
                }
                """,
            JsonUtils.objectToJson(commentRequest),
            true);
    }

    @Test
    void toComment() throws JSONException {
        CommentRequest commentRequest = createCommentRequest();
        Comment comment = commentRequest.toComment();
        assertThat(comment.getMetadata().getName()).isNotNull();

        comment.getMetadata().setName("fake");
        JSONAssert.assertEquals("""
                {
                    "spec": {
                        "raw": "raw",
                        "content": "content",
                        "allowNotification": true,
                        "subjectRef": {
                            "group": "fake.halo.run",
                            "version": "v1alpha1",
                            "kind": "Fake",
                            "name": "fake"
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

    private static CommentRequest createCommentRequest() {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setRaw("raw");
        commentRequest.setContent("content");
        commentRequest.setAllowNotification(true);

        FakeExtension fakeExtension = new FakeExtension();
        fakeExtension.setMetadata(new Metadata());
        fakeExtension.getMetadata().setName("fake");
        commentRequest.setSubjectRef(Ref.of(fakeExtension));
        return commentRequest;
    }
}