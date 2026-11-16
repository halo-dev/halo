package run.halo.app.localization;

import java.util.List;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Menu;
import run.halo.app.extension.GroupVersionKind;

/**
 * Display-only fields; identifiers, routing keys and relationships remain source-owned.
 *
 * @since 2.28.0
 */
@Component
public class MenuLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(Menu.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of(new LocalizableField("displayName", "/spec/displayName", LocalizableField.Format.PLAIN_TEXT));
    }
}
