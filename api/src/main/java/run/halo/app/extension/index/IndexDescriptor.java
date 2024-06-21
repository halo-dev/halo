package run.halo.app.extension.index;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class IndexDescriptor {

    private final IndexSpec spec;

    /**
     * Record whether the index is ready, managed by {@code IndexBuilder}.
     */
    private boolean ready;

    public IndexDescriptor(IndexSpec spec) {
        this.spec = spec;
    }
}
