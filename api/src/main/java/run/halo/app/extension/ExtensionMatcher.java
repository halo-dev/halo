package run.halo.app.extension;

import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

public interface ExtensionMatcher {
    GroupVersionKind getGvk();

    LabelSelector getLabelSelector();

    FieldSelector getFieldSelector();

    boolean match(Extension extension);
}
