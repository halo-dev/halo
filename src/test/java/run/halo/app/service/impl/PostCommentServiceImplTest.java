package run.halo.app.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.repository.PostCommentRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.CommentBlackListService;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentServiceImplTest {

    private static final String POST_COMMENT_CONTENT_TEST = "TestContent";

    private static final String POST_COMMENT_AUTHOR_TEST = "TestAuthor";

    private static final String POST_COMMENT_GRAVATAR_MD5_TEST = "d41d8cd98f00b204e9800998ecf8427e";

    private static final String GRAVATAR_SOURCE_TEST = "//example.com/avatar/";

    private static final String GRAVATAR_DEFAULT_TEST = "";

    @Mock
    private PostCommentRepository mockPostCommentRepository;

    @Mock
    private PostRepository mockPostRepository;

    @Mock
    private UserService mockUserService;

    @Mock
    private OptionService mockOptionService;

    @Mock
    private CommentBlackListService mockCommentBlackListService;

    @Mock
    private ApplicationEventPublisher mockApplicationEventPublisher;

    @Mock
    private PostComment mockPostComment;

    private PostCommentServiceImpl postCommentService;

    private PostCommentParam postCommentParam;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        postCommentService = new PostCommentServiceImpl(
            mockPostCommentRepository,
            mockPostRepository,
            mockUserService,
            mockOptionService,
            mockCommentBlackListService,
            mockApplicationEventPublisher
        );
        postCommentParam = new PostCommentParam();
    }

    @Test
    public void createBy_whenPostCommentParamHasNullEmailAndCommentNeedAudit_ReturnPostComment() {
        // Arrange
        postCommentParam.setContent(POST_COMMENT_CONTENT_TEST);
        postCommentParam.setAuthor(POST_COMMENT_AUTHOR_TEST);
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK,
            Boolean.class, true)).thenReturn(true);
        when(mockPostComment.getId()).thenReturn(1L);
        when(mockPostCommentRepository.save(any(PostComment.class))).thenReturn(mockPostComment);

        // Act
        PostComment result = postCommentService.createBy(postCommentParam);

        // Assert
        Assertions.assertEquals(mockPostComment, result);
    }

    @Test
    public void createBy_whenPostCommentParamHasNullEmailAndCommentDONOTNeedAudit_ReturnPostComment() {
        // Arrange
        postCommentParam.setContent(POST_COMMENT_CONTENT_TEST);
        postCommentParam.setAuthor(POST_COMMENT_AUTHOR_TEST);
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK,
            Boolean.class, true)).thenReturn(false);
        when(mockPostComment.getId()).thenReturn(1L);
        when(mockPostCommentRepository.save(any(PostComment.class))).thenReturn(mockPostComment);

        // Act
        PostComment result = postCommentService.createBy(postCommentParam);

        // Assert
        Assertions.assertEquals(mockPostComment, result);
    }

    @Test
    public void convertTo_whenPostCommentHasNullEmail_returnBaseCommentDTOWithAvatar() {
        // Arrange
        when(mockPostComment.getGravatarMd5()).thenReturn(POST_COMMENT_GRAVATAR_MD5_TEST);
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_SOURCE,
            String.class)).thenReturn(GRAVATAR_SOURCE_TEST);
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_DEFAULT,
            String.class)).thenReturn(GRAVATAR_DEFAULT_TEST);

        // Act
        BaseCommentDTO result = postCommentService.convertTo(mockPostComment);

        // Assert
        Assertions.assertEquals(
            GRAVATAR_SOURCE_TEST + POST_COMMENT_GRAVATAR_MD5_TEST + "?s=256&d=" +
                GRAVATAR_DEFAULT_TEST, result.getAvatar());
    }
}
