package run.halo.app.extension.index;

import java.util.List;

/**
 * Serializable snapshot of a single index. Keys are stored as canonical string parts (one part for value indexes, two
 * parts {@code [labelKey, labelValue]} for the label index).
 *
 * @since 2.26.0
 */
public record IndexSnapshot(
        String name, String fingerprint, String keyType, List<Entry> entries, List<String> nullKeys) {

    public record Entry(List<String> keyParts, List<String> primaryKeys) {}
}
