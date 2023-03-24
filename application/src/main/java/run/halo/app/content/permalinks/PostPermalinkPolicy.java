package run.halo.app.content.permalinks;

import static org.springframework.web.util.UriUtils.encode;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.PathUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class PostPermalinkPolicy implements PermalinkPolicy<Post> {
    public static final String DEFAULT_PERMALINK_PATTERN =
        SystemSetting.ThemeRouteRules.empty().getPost();
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public String permalink(Post post) {
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(post);
        String permalinkPattern =
            annotations.getOrDefault(Constant.PERMALINK_PATTERN_ANNO, DEFAULT_PERMALINK_PATTERN);
        return createPermalink(post, permalinkPattern);
    }

    public String pattern() {
        return environmentFetcher.fetchRouteRules()
            .map(SystemSetting.ThemeRouteRules::getPost)
            .blockOptional()
            .orElse(DEFAULT_PERMALINK_PATTERN);
    }

    private String createPermalink(Post post, String pattern) {
        Instant archiveTime = post.getSpec().getPublishTime();
        if (archiveTime == null) {
            archiveTime = post.getMetadata().getCreationTimestamp();
        }
        ZonedDateTime zonedDateTime = archiveTime.atZone(ZoneId.systemDefault());
        Properties properties = new Properties();
        properties.put("name", post.getMetadata().getName());
        properties.put("slug", encode(post.getSpec().getSlug(), StandardCharsets.UTF_8));
        properties.put("year", String.valueOf(zonedDateTime.getYear()));
        properties.put("month", NUMBER_FORMAT.format(zonedDateTime.getMonthValue()));
        properties.put("day", NUMBER_FORMAT.format(zonedDateTime.getDayOfMonth()));

        String simplifiedPattern = PathUtils.simplifyPathPattern(pattern);
        String permalink =
            PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(simplifiedPattern, properties);
        return externalUrlSupplier.get()
            .resolve(permalink)
            .normalize()
            .toString();
    }
}
