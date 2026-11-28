package run.halo.app.localization;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/** Declares the public metadata of built-in author pages. */
@Component
public class UserLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(User.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of();
    }

    @Override
    public boolean isEligible(Extension resource) {
        return resource instanceof User user
                && user.getSpec() != null
                && user.getMetadata() != null
                && user.getMetadata().getDeletionTimestamp() == null;
    }

    @Override
    public boolean publiclyVisible(Extension resource) {
        return resource instanceof User user
                && isEligible(resource)
                && user.getStatus() != null
                && !Objects.toString(user.getStatus().getPermalink(), "").isBlank();
    }

    @Override
    public String permalink(Extension resource) {
        if (!(resource instanceof User user) || user.getStatus() == null) {
            return "";
        }
        return Objects.toString(user.getStatus().getPermalink(), "");
    }
}
