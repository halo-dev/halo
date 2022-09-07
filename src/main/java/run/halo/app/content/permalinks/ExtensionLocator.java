package run.halo.app.content.permalinks;

import java.util.Objects;
import run.halo.app.extension.GroupVersionKind;

/**
 * Slug can be modified, so it is not included in {@link #equals(Object)} and {@link #hashCode()}.
 *
 * @param gvk group version kind
 * @param name extension name
 * @param slug extension slug
 */
public record ExtensionLocator(GroupVersionKind gvk, String name, String slug) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExtensionLocator locator = (ExtensionLocator) o;
        return gvk.equals(locator.gvk) && name.equals(locator.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gvk, name);
    }
}
