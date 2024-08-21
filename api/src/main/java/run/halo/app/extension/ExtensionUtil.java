package run.halo.app.extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryFactory;

public enum ExtensionUtil {
    ;

    public static boolean isDeleted(ExtensionOperator extension) {
        return extension.getMetadata() != null
            && extension.getMetadata().getDeletionTimestamp() != null;
    }

    public static boolean addFinalizers(MetadataOperator metadata, Set<String> finalizers) {
        var modifiableFinalizers = new HashSet<>(
            metadata.getFinalizers() == null ? Collections.emptySet() : metadata.getFinalizers());
        var added = modifiableFinalizers.addAll(finalizers);
        if (added) {
            metadata.setFinalizers(modifiableFinalizers);
        }
        return added;
    }

    public static boolean removeFinalizers(MetadataOperator metadata, Set<String> finalizers) {
        if (metadata.getFinalizers() == null) {
            return false;
        }
        var existingFinalizers = new HashSet<>(metadata.getFinalizers());
        var removed = existingFinalizers.removeAll(finalizers);
        if (removed) {
            metadata.setFinalizers(existingFinalizers);
        }
        return removed;
    }

    /**
     * Query for not deleting.
     *
     * @return Query
     */
    public static Query notDeleting() {
        return QueryFactory.isNull("metadata.deletionTimestamp");
    }

    /**
     * Default sort by creation timestamp desc and name asc.
     *
     * @return Sort
     */
    public static Sort defaultSort() {
        return Sort.by(
            Sort.Order.desc("metadata.creationTimestamp"),
            Sort.Order.asc("metadata.name")
        );
    }

}
