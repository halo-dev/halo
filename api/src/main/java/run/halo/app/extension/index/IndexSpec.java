package run.halo.app.extension.index;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IndexSpec {
    private String name;

    private IndexAttribute indexFunc;

    private OrderType order;

    private boolean unique;

    public enum OrderType {
        ASC,
        DESC
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
}
