package run.halo.app.extension.index;

import java.util.List;

/** A complete type snapshot bound to a clean database checkpoint. */
public record IndicesSnapshot(String checkpoint, List<IndexSnapshot> indices) {
    public IndicesSnapshot(List<IndexSnapshot> indices) {
        this("", indices);
    }
}
