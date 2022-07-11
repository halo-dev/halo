package run.halo.app.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

/**
 * <p>ConfigMap holds configuration data to consume.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = "ConfigMap", plural = "configmaps",
    singular = "configmap")
public class ConfigMap extends AbstractExtension {

    private Map<String, String> data;

    public ConfigMap putDataItem(String key, String dataItem) {
        if (this.data == null) {
            this.data = new LinkedHashMap<>();
        }
        this.data.put(key, dataItem);
        return this;
    }

    @Nullable
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "class ConfigMap {\n"
            + "    apiVersion: " + toIndentedString(getApiVersion()) + "\n"
            + "    data: " + toIndentedString(data) + "\n"
            + "    kind: " + toIndentedString(getKind()) + "\n"
            + "    metadata: " + toIndentedString(getMetadata()) + "\n"
            + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
