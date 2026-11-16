package run.halo.app.localization;

import java.util.List;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersionKind;

/**
 * Only the system site's explicit public presentation fields; never arbitrary ConfigMap data.
 *
 * @since 2.28.0
 */
@Component
public class SiteLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(ConfigMap.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of(
                new LocalizableField("title", null, LocalizableField.Format.PLAIN_TEXT),
                new LocalizableField("subtitle", null, LocalizableField.Format.PLAIN_TEXT),
                new LocalizableField("seo.description", null, LocalizableField.Format.PLAIN_TEXT),
                new LocalizableField("seo.keywords", null, LocalizableField.Format.PLAIN_TEXT));
    }
}
