package run.halo.app.extension;

import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

@Data
@Accessors(chain = true)
public class ListOptions {
    private LabelSelector labelSelector;
    private FieldSelector fieldSelector;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (fieldSelector != null) {
            sb.append("fieldSelector: ").append(fieldSelector.query());
        }
        if (labelSelector != null) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append("labelSelector: ").append(labelSelector);
        }
        return sb.toString();
    }
}
