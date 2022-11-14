package run.halo.app.extension;

import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Extension utilities.
 *
 * @author johnniang
 */
public final class ExtensionUtil {

    private ExtensionUtil() {
    }

    /**
     * Builds the name prefix of ExtensionStore.
     *
     * @param scheme is scheme of an Extension.
     * @return name prefix of ExtensionStore.
     */
    public static String buildStoreNamePrefix(Scheme scheme) {
        // rule of key: /registry/[group]/plural-name/extension-name
        StringBuilder builder = new StringBuilder("/registry/");
        if (StringUtils.hasText(scheme.groupVersionKind().group())) {
            builder.append(scheme.groupVersionKind().group()).append('/');
        }
        builder.append(scheme.plural());
        return builder.toString();
    }

    /**
     * Builds full name of ExtensionStore.
     *
     * @param scheme is scheme of an Extension.
     * @param name the exact name of Extension.
     * @return full name of ExtensionStore.
     */
    public static String buildStoreName(Scheme scheme, String name) {
        return buildStoreNamePrefix(scheme) + "/" + name;
    }

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
            extension.getMetadata().setLabels(annotations);
        }
        return annotations;
    }
}
