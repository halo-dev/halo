package run.halo.app.localization;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/** Declares the public metadata of built-in tag archive pages. */
@Component
public class TagLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return Tag.GVK;
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of();
    }

    @Override
    public boolean isEligible(Extension resource) {
        return resource instanceof Tag tag
                && tag.getSpec() != null
                && tag.getMetadata() != null
                && tag.getMetadata().getDeletionTimestamp() == null;
    }

    @Override
    public boolean publiclyVisible(Extension resource) {
        return resource instanceof Tag tag
                && isEligible(resource)
                && tag.getStatus() != null
                && !Objects.toString(tag.getStatus().getPermalink(), "").isBlank();
    }

    @Override
    public String permalink(Extension resource) {
        if (!(resource instanceof Tag tag) || tag.getStatus() == null) {
            return "";
        }
        return Objects.toString(tag.getStatus().getPermalink(), "");
    }
}
