package run.halo.app.content.comment;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link CommentEmailOwner}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CommentEmailOwnerTest {

    @Test
    void constructorTest() throws JSONException {
        CommentEmailOwner commentEmailOwner =
            new CommentEmailOwner("example@example.com", "avatar", "displayName", "website");
        JSONAssert.assertEquals("""
                {
                    "email": "example@example.com",
                    "avatar": "avatar",
                    "displayName": "displayName",
                    "website": "website"
                }
                """,
            JsonUtils.objectToJson(commentEmailOwner),
            true);
    }

    @Test
    void toCommentOwner() throws JSONException {
        CommentEmailOwner commentEmailOwner =
            new CommentEmailOwner("example@example.com", "avatar", "displayName", "website");
        Comment.CommentOwner commentOwner = commentEmailOwner.toCommentOwner();
        JSONAssert.assertEquals("""
                {
                    "kind": "Email",
                    "name": "example@example.com",
                    "displayName": "displayName",
                    "annotations": {
                        "website": "website",
                        "avatar": "avatar"
                    }
                }
                """,
            JsonUtils.objectToJson(commentOwner),
            true);
    }
}