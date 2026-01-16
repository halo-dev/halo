package run.halo.app.content.permalinks;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.infra.utils.ReactiveUtils;
import run.halo.app.theme.utils.PatternUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class TagPermalinkPolicy implements PermalinkPolicy<Tag> {

    private static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private static final String DEFAULT_PERMALINK_PREFIX =
        SystemSetting.ThemeRouteRules.empty().getTags();
    private final ExternalUrlSupplier externalUrlSupplier;
    private final SystemConfigFetcher environmentFetcher;

    @Override
    public String permalink(Tag tag) {
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(tag);
        String permalinkPrefix =
            annotations.getOrDefault(Constant.PERMALINK_PATTERN_ANNO, DEFAULT_PERMALINK_PREFIX);

        String slug = UriUtils.encode(tag.getSpec().getSlug(), StandardCharsets.UTF_8);
        String path = PathUtils.combinePath(permalinkPrefix, slug);
        return externalUrlSupplier.get()
            .resolve(path)
            .normalize().toString();
    }

    public String pattern() {
        return environmentFetcher.fetchRouteRules()
            .map(SystemSetting.ThemeRouteRules::getTags)
            .defaultIfEmpty(DEFAULT_PERMALINK_PREFIX)
            .map(PatternUtils::normalizePattern)
            .block(BLOCKING_TIMEOUT);
    }
}
