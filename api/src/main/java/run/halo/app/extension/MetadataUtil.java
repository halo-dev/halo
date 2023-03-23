package run.halo.app.extension;

import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;

public enum MetadataUtil {
    ;

    /**
     * Gets extension metadata labels null safe.
     *
     * @param extension extension must not be null
     * @return extension metadata labels
     */
    public static Map<String, String> nullSafeLabels(AbstractExtension extension) {
        Assert.notNull(extension, "The extension must not be null.");
        Assert.notNull(extension.getMetadata(), "The extension metadata must not be null.");
        Map<String, String> labels = extension.getMetadata().getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            extension.getMetadata().setLabels(labels);
        }
        return labels;
    }

    /**
     * Gets extension metadata annotations null safe.
     *
     * @param extension extension must not be null
     * @return extension metadata annotations
     */
    public static Map<String, String> nullSafeAnnotations(AbstractExtension extension) {
        Assert.notNull(extension, "The extension must not be null.");
        Assert.notNull(extension.getMetadata(), "The extension metadata must not be null.");
        Map<String, String> annotations = extension.getMetadata().getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>();
            extension.getMetadata().setAnnotations(annotations);
        }
        return annotations;
    }
}
