package run.halo.app.content.permalinks;

import static org.springframework.web.util.UriUtils.encode;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
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
public class CategoryPermalinkPolicy implements PermalinkPolicy<Category> {
    public static final String DEFAULT_PERMALINK_PREFIX =
        SystemSetting.ThemeRouteRules.empty().getCategories();

    private final ExternalUrlSupplier externalUrlSupplier;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public String permalink(Category category) {
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(category);
        String permalinkPrefix =
            annotations.getOrDefault(Constant.PERMALINK_PATTERN_ANNO, DEFAULT_PERMALINK_PREFIX);
        String slug = encode(category.getSpec().getSlug(), StandardCharsets.UTF_8);
        String path = PathUtils.combinePath(permalinkPrefix, slug);
        return externalUrlSupplier.get()
            .resolve(path)
            .normalize().toString();
    }

    public String pattern() {
        return environmentFetcher.fetchRouteRules()
            .map(SystemSetting.ThemeRouteRules::getCategories)
            .blockOptional()
            .orElse(DEFAULT_PERMALINK_PREFIX);
    }
}
