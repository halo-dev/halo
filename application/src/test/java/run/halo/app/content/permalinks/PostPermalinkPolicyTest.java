package run.halo.app.content.permalinks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.utils.PathUtils;

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
    private ApplicationContext applicationContext;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    private PostPermalinkPolicy postPermalinkPolicy;

    @BeforeEach
    void setUp() {
        lenient().when(externalUrlSupplier.get()).thenReturn(URI.create(""));
        postPermalinkPolicy = new PostPermalinkPolicy(environmentFetcher, externalUrlSupplier);
    }

    @Test
    void permalink() {
        Post post = TestPost.postV1();
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(post);
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/{year}/{month}/{day}/{slug}");
        post.getMetadata().setName("test-post");
        post.getSpec().setSlug("test-post-slug");
        Instant now = Instant.now();
        post.getSpec().setPublishTime(now);

        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        String year = String.valueOf(zonedDateTime.getYear());
        String month = NUMBER_FORMAT.format(zonedDateTime.getMonthValue());
        String day = NUMBER_FORMAT.format(zonedDateTime.getDayOfMonth());

        String permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink)
            .isEqualTo(PathUtils.combinePath(year, month, day, post.getSpec().getSlug()));

        // pattern {month}/{day}/{slug}
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/{month}/{day}/{slug}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink)
            .isEqualTo(PathUtils.combinePath(month, day, post.getSpec().getSlug()));

        // pattern /?p={name}
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/?p={name}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/?p=test-post");

        // pattern /posts/{slug}
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/posts/{slug}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/posts/test-post-slug");

        // pattern /posts/{name}
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/posts/{name}");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("/posts/test-post");
    }

    @Test
    void permalinkWithExternalUrl() {
        Post post = TestPost.postV1();
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(post);
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, "/{year}/{month}/{day}/{slug}");
        post.getMetadata().setName("test-post");
        post.getSpec().setSlug("test-post-slug");
        Instant now = Instant.parse("2022-11-01T02:40:06.806310Z");
        post.getSpec().setPublishTime(now);

        when(externalUrlSupplier.get()).thenReturn(URI.create("http://example.com"));

        String permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("http://example.com/2022/11/01/test-post-slug");

        post.getSpec().setSlug("中文 slug");
        permalink = postPermalinkPolicy.permalink(post);
        assertThat(permalink).isEqualTo("http://example.com/2022/11/01/%E4%B8%AD%E6%96%87%20slug");
    }
}