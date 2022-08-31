package run.halo.app.content.permalinks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Post;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkPatternProvider;

/**
 * Tests for {@link PostPermalinkPolicy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostPermalinkPolicyTest {
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");

    @Mock
    private PermalinkPatternProvider permalinkPatternProvider;

    @Mock
    private ApplicationContext applicationContext;

    private PostPermalinkPolicy postPermalinkPolicy;

    @BeforeEach
    void setUp() {
        postPermalinkPolicy = new PostPermalinkPolicy(permalinkPatternProvider, applicationContext);
    }

    @Test
    void permalink() {
        Post post = TestPost.postV1();
        post.getMetadata().setName("test-post");
        post.getSpec().setSlug("test-post-slug");
        Instant now = Instant.now();
        post.getSpec().setPublishTime(now);

        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        String year = String.valueOf(zonedDateTime.getYear());
        String month = NUMBER_FORMAT.format(zonedDateTime.getMonthValue());
        String day = NUMBER_FORMAT.format(zonedDateTime.getDayOfMonth());

        // pattern /{year}/{month}/{day}/{slug}
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/{year}/{month}/{day}/{slug}");
        String permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink)
            .isEqualTo(PathUtils.combinePath(year, month, day, post.getSpec().getSlug()));

        // pattern {month}/{day}/{slug}
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/{month}/{day}/{slug}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink)
            .isEqualTo(PathUtils.combinePath(month, day, post.getSpec().getSlug()));

        // pattern /?p={name}
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/?p={name}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/?p=test-post");

        // pattern /posts/{slug}
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/posts/{slug}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/posts/test-post-slug");

        // pattern /posts/{name}
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/posts/{name}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/posts/test-post");
    }

    @Test
    void templateName() {
        String s = postPermalinkPolicy.templateName();
        assertThat(s).isEqualTo(DefaultTemplateEnum.POST.getValue());
    }

    @Test
    void pattern() {
        when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST))
            .thenReturn("/{year}/{month}/{day}/{slug}");
        assertThat(postPermalinkPolicy.pattern()).isEqualTo("/{year}/{month}/{day}/{slug}");
    }
}