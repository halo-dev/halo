package run.halo.app.localization;

import java.util.List;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.GroupVersionKind;

/**
 * Display-only fields; identifiers, routing keys and relationships remain source-owned.
 *
 * @since 2.28.0
 */
@Component
public class TagLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(Tag.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of(
                new LocalizableField("displayName", "/spec/displayName", LocalizableField.Format.PLAIN_TEXT),
                new LocalizableField("description", "/spec/description", LocalizableField.Format.PLAIN_TEXT));
    }
}
