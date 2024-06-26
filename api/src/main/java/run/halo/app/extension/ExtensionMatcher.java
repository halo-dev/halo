package run.halo.app.extension;

import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

public interface ExtensionMatcher {
    @Deprecated(since = "2.17.0", forRemoval = true)
    default GroupVersionKind getGvk() {
        return null;
    }

    @Deprecated(since = "2.17.0", forRemoval = true)
    default LabelSelector getLabelSelector() {
        return null;
    }

    @Deprecated(since = "2.17.0", forRemoval = true)
    default FieldSelector getFieldSelector() {
        return null;
    }

    boolean match(Extension extension);
}
