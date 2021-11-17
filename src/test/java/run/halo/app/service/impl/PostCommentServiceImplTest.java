package run.halo.app.service.impl;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.repository.PostCommentRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.CommentBlackListService;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentServiceImplTest {

    private static final String GRAVATAR_SOURCE_TEST = "//example.com/avatar/";

    private static final String GRAVATAR_IDENTICON_TEST = "identicon";

    private static final String GRAVATAR_MD5_TEST = "Test";

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

    private PostCommentServiceImpl postCommentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        postCommentService = new PostCommentServiceImpl(
            mockPostCommentRepository,
            mockPostRepository,
            mockUserService,
            mockOptionService,
            mockCommentBlackListService,
            mockApplicationEventPublisher);

        when(mockOptionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_SOURCE,
            String.class)).thenReturn(GRAVATAR_SOURCE_TEST);
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_DEFAULT,
            String.class)).thenReturn("");
        when(mockOptionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_IDENTICON,
            String.class)).thenReturn(GRAVATAR_IDENTICON_TEST);
    }

    @Test
    public void buildAvatarUrl_whenEmailIsNotEmpty_ReturnGravatarUrlWithoutIdenticon() {
        // Act
        String avatarUrl = postCommentService.buildAvatarUrl(GRAVATAR_MD5_TEST, false);

        // Assert
        Assertions.assertEquals(GRAVATAR_SOURCE_TEST + GRAVATAR_MD5_TEST + "?s=256&d=",
            avatarUrl);
    }

    @Test
    public void buildAvatarUrl_whenEmailIsEmpty_ReturnGravatarUrlWithIdenticon() {
        // Act
        String avatarUrl = postCommentService.buildAvatarUrl(GRAVATAR_MD5_TEST, true);

        // Assert
        Assertions.assertEquals(
            GRAVATAR_SOURCE_TEST + GRAVATAR_MD5_TEST + "?s=256&d=" + GRAVATAR_IDENTICON_TEST,
            avatarUrl);
    }
}
