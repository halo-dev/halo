package run.halo.app.extension;

import java.util.HashSet;
import java.util.Set;

public enum ExtensionUtil {
    ;

    public static boolean isDeleted(ExtensionOperator extension) {
        return extension.getMetadata() != null
            && extension.getMetadata().getDeletionTimestamp() != null;
    }

    public static void addFinalizers(MetadataOperator metadata, Set<String> finalizers) {
        var existingFinalizers = metadata.getFinalizers();
        if (existingFinalizers == null) {
            existingFinalizers = new HashSet<>();
        }
        existingFinalizers.addAll(finalizers);
        metadata.setFinalizers(existingFinalizers);
    }

    public static void removeFinalizers(MetadataOperator metadata, Set<String> finalizers) {
        var existingFinalizers = metadata.getFinalizers();
        if (existingFinalizers != null) {
            existingFinalizers.removeAll(finalizers);
        }
        metadata.setFinalizers(existingFinalizers);
    }

}
