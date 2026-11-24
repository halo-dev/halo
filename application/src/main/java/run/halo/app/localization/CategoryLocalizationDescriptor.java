package run.halo.app.localization;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/** Declares the public metadata of built-in category archive pages. */
@Component
public class CategoryLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return Category.GVK;
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of();
    }

    @Override
    public boolean isEligible(Extension resource) {
        return resource instanceof Category category
                && category.getSpec() != null
                && category.getMetadata() != null
                && category.getMetadata().getDeletionTimestamp() == null;
    }

    @Override
    public boolean publiclyVisible(Extension resource) {
        return resource instanceof Category category
                && isEligible(resource)
                && category.getStatus() != null
                && !Objects.toString(category.getStatus().getPermalink(), "").isBlank();
    }

    @Override
    public String permalink(Extension resource) {
        if (!(resource instanceof Category category) || category.getStatus() == null) {
            return "";
        }
        return Objects.toString(category.getStatus().getPermalink(), "");
    }
}
