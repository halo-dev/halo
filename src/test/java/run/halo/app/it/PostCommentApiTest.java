package run.halo.app.it;

import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.model.support.BaseResponse;

public class PostCommentApiTest extends BaseApiTest {

    private static final String COMMENT_NULL_EMAIL_GRAVATAR_MD5 =
        "d41d8cd98f00b204e9800998ecf8427e";

    private static final String COMMENT_NULL_EMAIL_AVATAR =
        "//gravatar.com/avatar/" + COMMENT_NULL_EMAIL_GRAVATAR_MD5 + "?s=256&d=";

    private static final String COMMENT_AUTHOR_TEST = "TestAuthor";

    private static final String COMMENT_CONTENT_TEST = "TestContent";

    private static final String COMMENT_VALID_EMAIL_TEST = "test@example.com";

    private static final String COMMENT_INVALID_EMAIL_TEST = "hello world";

    private static final Integer COMMENT_POST_ID_TEST = 1;

    private static final String COMMENT_API_ROUTE = "/api/content/posts/comments";

    private static final String VALIDATION_VIOLATION_MESSAGE = "字段验证错误，请完善后重试！";

    private PostCommentParam postCommentParam;

    @BeforeEach
    private void setUp() {
        installBlog();
        postCommentParam = new PostCommentParam();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void testCommentWithNullEmail() {
        // Arrange
        postCommentParam.setContent(COMMENT_CONTENT_TEST);
        postCommentParam.setAuthor(COMMENT_AUTHOR_TEST);
        postCommentParam.setPostId(COMMENT_POST_ID_TEST);
        HttpEntity<PostCommentParam> request = new HttpEntity<>(postCommentParam);

        // Act
        ResponseEntity<BaseResponse<BaseCommentDTO>>
            result = restTemplate.exchange(blogUrl + COMMENT_API_ROUTE, HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {}
            );
        BaseCommentDTO comment = Objects.requireNonNull(result.getBody()).getData();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(COMMENT_AUTHOR_TEST, comment.getAuthor());
        Assertions.assertEquals(COMMENT_CONTENT_TEST, comment.getContent());
        Assertions.assertNull(comment.getEmail());
        Assertions.assertEquals(COMMENT_NULL_EMAIL_GRAVATAR_MD5, comment.getGravatarMd5());
        Assertions.assertEquals(COMMENT_NULL_EMAIL_AVATAR, comment.getAvatar());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void testCommentWithInvalidEmail() {
        // Arrange
        postCommentParam.setContent(COMMENT_CONTENT_TEST);
        postCommentParam.setAuthor(COMMENT_AUTHOR_TEST);
        postCommentParam.setPostId(COMMENT_POST_ID_TEST);
        postCommentParam.setEmail(COMMENT_INVALID_EMAIL_TEST);
        HttpEntity<PostCommentParam> request = new HttpEntity<>(postCommentParam);

        // Act
        ResponseEntity<BaseResponse<BaseCommentDTO>>
            result = restTemplate.exchange(blogUrl + COMMENT_API_ROUTE, HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {}
            );

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertEquals(VALIDATION_VIOLATION_MESSAGE,
            Objects.requireNonNull(result.getBody()).getMessage());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void testCommentWithValidEmail() {
        // Arrange
        postCommentParam.setContent(COMMENT_CONTENT_TEST);
        postCommentParam.setAuthor(COMMENT_AUTHOR_TEST);
        postCommentParam.setPostId(COMMENT_POST_ID_TEST);
        postCommentParam.setEmail(COMMENT_VALID_EMAIL_TEST);
        HttpEntity<PostCommentParam> request = new HttpEntity<>(postCommentParam);

        // Act
        ResponseEntity<BaseResponse<BaseCommentDTO>>
            result = restTemplate.exchange(blogUrl + COMMENT_API_ROUTE, HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {}
            );
        BaseCommentDTO comment = Objects.requireNonNull(result.getBody()).getData();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(COMMENT_AUTHOR_TEST, comment.getAuthor());
        Assertions.assertEquals(COMMENT_CONTENT_TEST, comment.getContent());
        Assertions.assertNull(comment.getEmail());
    }
}
