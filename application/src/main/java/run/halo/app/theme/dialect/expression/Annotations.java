package run.halo.app.theme.dialect.expression;

import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;

/**
 * <p>Expression Object for performing annotations operations inside Halo Extra Expressions.</p>
 * An object of this class is usually available in variable evaluation expressions with the name
 * <code>#annotations</code>.
 *
 * @author guqing
 * @since 2.0.2
 */
public class Annotations {

    /**
     * Get annotation value from extension vo.
     *
     * @param extension extension vo
     * @param key the key of annotation
     * @return annotation value if exists, otherwise null
     */
    @Nullable
    public String get(ExtensionVoOperator extension, String key) {
        Map<String, String> annotations = extension.getMetadata().getAnnotations();
        if (annotations == null) {
            return null;
        }
        return annotations.get(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or defaultValue if
     * <code>extension</code> contains no mapping for the key.
     *
     * @param extension extension vo
     * @param key the key of annotation
     * @return annotation value if exists, otherwise defaultValue
     */
    @NonNull
    public String getOrDefault(ExtensionVoOperator extension, String key, String defaultValue) {
        Map<String, String> annotations = extension.getMetadata().getAnnotations();
        if (annotations == null) {
            return defaultValue;
        }
        return annotations.getOrDefault(key, defaultValue);
    }

    /**
     * Check if the extension has the specified annotation.
     *
     * @param extension extension vo
     * @param key the key of annotation
     * @return true if the extension has the specified annotation, otherwise false
     */
    public boolean contains(ExtensionVoOperator extension, String key) {
        Map<String, String> annotations = extension.getMetadata().getAnnotations();
        if (annotations == null) {
            return false;
        }
        return annotations.containsKey(key);
    }
}
