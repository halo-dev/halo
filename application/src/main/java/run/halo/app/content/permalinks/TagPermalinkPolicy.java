package run.halo.app.content.permalinks;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Tag;
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
public class TagPermalinkPolicy implements PermalinkPolicy<Tag> {
    public static final String DEFAULT_PERMALINK_PREFIX =
        SystemSetting.ThemeRouteRules.empty().getTags();
    private final ExternalUrlSupplier externalUrlSupplier;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

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
            .blockOptional()
            .orElse(DEFAULT_PERMALINK_PREFIX);
    }
}
