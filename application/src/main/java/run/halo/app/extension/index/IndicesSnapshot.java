package run.halo.app.extension.index;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of all indices of one extension type, plus the {@code name -> version} manifest used as the watermark for
 * delta recovery.
 *
 * @since 2.26.0
 */
public record IndicesSnapshot(List<IndexSnapshot> indices, Map<String, Long> versions) {}
