package run.halo.app.extension.index;

import com.google.common.base.Objects;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;

/**
 * An implementation of {@link MultiValueIndexSpec}.
 *
 * @param <E> the type of the extension
 * @param <K> the type of the key
 * @deprecated Use {@link IndexSpecs#multi(String, Class)} instead.
 */
@Data
@Accessors(chain = true)
@Deprecated(forRemoval = true, since = "2.22.0")
public class IndexSpec<E extends Extension, K extends Comparable<K>>
    implements ValueIndexSpec<E, K> {

    private String name;

    private IndexAttribute<E, K> indexFunc;

    private OrderType order;

    private boolean unique;

    public Set<K> getValues(E extension) {
        return indexFunc.getValues(extension);
    }

    public enum OrderType {
        ASC,
        DESC
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public Class<K> getKeyType() {
        return indexFunc.getKeyType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexSpec indexSpec = (IndexSpec) o;
        return Objects.equal(name, indexSpec.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Normalize to single or multi value index spec.
     *
     * @return the normalized index spec
     */
    public ValueIndexSpec<E, K> normalize() {
        if (this.indexFunc.singleValue()) {
            return IndexSpecs.<E, K>single(name, getKeyType())
                .unique(unique)
                .indexFunc(e -> {
                    var values = getValues(e);
                    return CollectionUtils.isEmpty(values) ? null : values.iterator().next();
                })
                .build();
        }
        return IndexSpecs.<E, K>multi(name, getKeyType())
            .unique(unique)
            .indexFunc(this::getValues)
            .build();
    }
}
