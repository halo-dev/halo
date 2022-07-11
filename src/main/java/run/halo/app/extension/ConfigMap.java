package run.halo.app.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * <p>ConfigMap holds configuration data to consume.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@GVK(group = "", version = "v1alpha1", kind = "ConfigMap", plural = "configmaps",
    singular = "configmap")
public class ConfigMap extends AbstractExtension {

    private Map<String, String> data;

    public void setData(Map<String, String> data) {
        this.data = data;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ConfigMap configMap = (ConfigMap) o;
        return Objects.equals(data, configMap.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data);
    }
}
