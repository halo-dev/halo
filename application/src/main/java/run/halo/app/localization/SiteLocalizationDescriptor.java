package run.halo.app.localization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;

/**
 * Only the system site's explicit public presentation fields; never arbitrary ConfigMap data.
 *
 * @since 2.28.0
 */
@Component
public class SiteLocalizationDescriptor implements ResourceLocalizationDescriptor {
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String SEO_DESCRIPTION = "seo.description";
    public static final String SEO_KEYWORDS = "seo.keywords";

    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(ConfigMap.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return LocalizableField.list(
                LocalizableField.text(TITLE, null)
                        .label("core.localization.field.title")
                        .required(true),
                LocalizableField.text(SUBTITLE, null).label("core.localization.field.subtitle"),
                LocalizableField.text(SEO_DESCRIPTION, null)
                        .label("core.localization.field.seoDescription")
                        .multiline(true),
                LocalizableField.text(SEO_KEYWORDS, null).label("core.localization.field.seoKeywords"));
    }

    @Override
    public String displayName() {
        return "core.site_information.title";
    }

    @Override
    public String titleField() {
        return TITLE;
    }

    @Override
    public boolean isEligible(Extension resource) {
        return resource instanceof ConfigMap
                && SystemSetting.SYSTEM_CONFIG.equals(resource.getMetadata().getName());
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        return Mono.fromSupplier(() -> {
            var config = (ConfigMap) resource;
            var data = Objects.requireNonNullElse(config.getData(), Map.<String, String>of());
            var basic = SystemSetting.get(data, SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
            var seo = SystemSetting.get(data, SystemSetting.Seo.GROUP, SystemSetting.Seo.class);
            var fields = new LinkedHashMap<>(extracted);
            fields.put(TITLE, basic == null ? "" : Objects.toString(basic.getTitle(), ""));
            fields.put(SUBTITLE, basic == null ? "" : Objects.toString(basic.getSubtitle(), ""));
            fields.put(SEO_DESCRIPTION, seo == null ? "" : Objects.toString(seo.getDescription(), ""));
            fields.put(SEO_KEYWORDS, seo == null ? "" : Objects.toString(seo.getKeywords(), ""));
            return fields;
        });
    }
}
