package run.halo.app.controller.content.api;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCommentService;
import run.halo.app.service.PostService;

@SpringBootTest
@ActiveProfiles("test")
public class PostControllerTest {

    private static final String COMMENT_CONTENT_TEST = "Hello world!";

    @Mock
    private PostService mockPostService;

    @Mock
    private PostCommentService mockPostCommentService;

    @Mock
    private OptionService mockOptionService;

    @Mock
    private BaseCommentDTO mockBaseCommentDTO;

    @Mock
    private PostComment mockPostComment;

    private PostController postController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        postController = new PostController(
            mockPostService,
            mockPostCommentService,
            mockOptionService
        );
    }

    @Test
    public void nullEmailCommentTest() {
        // Arrange
        PostCommentParam postCommentParam = new PostCommentParam();
        postCommentParam.setContent(COMMENT_CONTENT_TEST);
        when(mockPostCommentService.createBy(postCommentParam)).thenReturn(mockPostComment);
        when(mockPostCommentService.convertTo(mockPostComment)).thenReturn(mockBaseCommentDTO);

        // Act
        BaseCommentDTO result = postController.comment(postCommentParam);

        // Assert
        Assertions.assertEquals(mockBaseCommentDTO, result);
    }
}
