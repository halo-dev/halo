package run.halo.app.extension;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.index.query.Query;

public enum ExtensionUtil {
    ;

    /**
     * Label to mark an extension resource should not be overwritten during initialization.
     */
    public static final String DO_NOT_OVERWRITE_LABEL = "halo.run/do-not-overwrite";

    /**
     * Check if the extension has the do-not-overwrite label. If the label is present and set to
     * true, it indicates that the extension should not be overwritten during initialization.
     *
     * @param extension the extension
     * @return true if it has the label, false otherwise
     */
    public static boolean hasDoNotOverwriteLabel(ExtensionOperator extension) {
        if (extension.getMetadata() == null) {
            return false;
        }
        var labels = extension.getMetadata().getLabels();
        return labels != null && Boolean.parseBoolean(labels.get(DO_NOT_OVERWRITE_LABEL));
    }

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
        return Queries.isNull("metadata.deletionTimestamp");
    }

    /**
     * Default sort by creation timestamp desc and name asc.
     *
     * @return Sort
     */
    public static Sort defaultSort() {
        return Sort.by(
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }

}
