package run.halo.app.extension.index;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.data.util.Pair;
import run.halo.app.extension.Extension;

@UtilityClass
public class LabelIndexSpecUtils {

    public static final String LABEL_PATH = "metadata.labels";

    /**
     * Creates a label index spec.
     *
     * @param extensionType extension type
     * @param <E> extension type
     * @return label index spec
     */
    public static <E extends Extension> IndexSpec labelIndexSpec(Class<E> extensionType) {
        return new IndexSpec()
            .setName(LABEL_PATH)
            .setOrder(IndexSpec.OrderType.ASC)
            .setUnique(false)
            .setIndexFunc(IndexAttributeFactory.multiValueAttribute(extensionType,
                LabelIndexSpecUtils::labelIndexValueFunc)
            );
    }

    /**
     * Label key-value pair from indexed label key string, e.g. "key=value".
     *
     * @param indexedLabelKey indexed label key
     * @return label key-value pair
     */
    public static Pair<String, String> labelKeyValuePair(String indexedLabelKey) {
        var idx = indexedLabelKey.indexOf('=');
        if (idx != -1) {
            return Pair.of(indexedLabelKey.substring(0, idx), indexedLabelKey.substring(idx + 1));
        }
        throw new IllegalArgumentException("Invalid label key-value pair: " + indexedLabelKey);
    }

    static <E extends Extension> Set<String> labelIndexValueFunc(E obj) {
        var labels = obj.getMetadata().getLabels();
        if (labels == null) {
            return Set.of();
        }
        return labels.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.toSet());
    }
}
