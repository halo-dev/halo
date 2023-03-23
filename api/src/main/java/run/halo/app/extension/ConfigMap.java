package run.halo.app.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>ConfigMap holds configuration data to consume.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = ConfigMap.KIND, plural = "configmaps",
    singular = "configmap")
public class ConfigMap extends AbstractExtension {

    public static final String KIND = "ConfigMap";

    private Map<String, String> data;

    public ConfigMap putDataItem(String key, String dataItem) {
        if (this.data == null) {
            this.data = new LinkedHashMap<>();
        }
        this.data.put(key, dataItem);
        return this;
    }
}
