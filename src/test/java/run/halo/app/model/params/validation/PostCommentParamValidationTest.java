package run.halo.app.model.params.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.params.PostCommentParam;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentParamValidationTest {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final String TEST_CONTENT = "TestContent";
    private static final String TEST_AUTHOR = "TestAuthor";
    private static final String TEST_INVALID_EMAIL = "example.com";

    private Validator validator;
    private PostCommentParam postCommentParam;

    @BeforeEach
    public void setUp() {
        validator = factory.getValidator();
        postCommentParam = new PostCommentParam();
    }

    @Test
    public void nullEmailValidationTest() {
        // Arrange
        setupDefaultValuesForPostCommentParam();

        // Act
        Set<ConstraintViolation<PostCommentParam>> violations =
            validator.validate(postCommentParam);

        // Assert
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    public void invalidEmailValidationTest() {
        // Arrange
        setupDefaultValuesForPostCommentParam();
        postCommentParam.setEmail(TEST_INVALID_EMAIL);

        // Act
        Set<ConstraintViolation<PostCommentParam>> violations =
            validator.validate(postCommentParam);

        // Assert
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("email", violations.iterator().next().getPropertyPath().toString());
        Assertions.assertEquals("邮箱格式不正确", violations.iterator().next().getMessage());
    }

    public void setupDefaultValuesForPostCommentParam() {
        postCommentParam.setContent(TEST_CONTENT);
        postCommentParam.setAuthor(TEST_AUTHOR);
    }
}
