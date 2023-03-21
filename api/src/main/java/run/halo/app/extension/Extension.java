package run.halo.app.extension;

import java.util.Comparator;
import java.util.Objects;

/**
 * Extension is an interface which represents an Extension. It contains setters and getters of
 * GroupVersionKind and Metadata.
 */
public interface Extension extends ExtensionOperator, Comparable<Extension> {

    @Override
    default int compareTo(Extension another) {
        if (another == null || another.getMetadata() == null) {
            return 1;
        }
        if (getMetadata() == null) {
            return -1;
        }
        return Objects.compare(getMetadata().getName(), another.getMetadata().getName(),
            Comparator.naturalOrder());
    }
}
