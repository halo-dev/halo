package run.halo.app.extension.index;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;

/**
 * Label index implementation.
 *
 * @param <E> the type of extension
 * @author johnniang
 * @since 2.22.0
 */
class LabelIndex<E extends Extension> implements LabelIndexQuery, Index<E, String> {

    private final ConcurrentNavigableMap<LabelEntry, Set<String>> index;

    private final ConcurrentMap<String, Set<LabelEntry>> invertedIndex;

    /**
     * Set of primary keys of extensions with empty labels.
     */
    private final Set<String> emptyLabelsSet;

    public LabelIndex() {
        this.index = new ConcurrentSkipListMap<>();
        this.invertedIndex = new ConcurrentHashMap<>();
        this.emptyLabelsSet = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void close() throws IOException {
        this.index.clear();
        this.invertedIndex.clear();
        this.emptyLabelsSet.clear();
    }

    @Override
    public String getName() {
        return "metadata.labels";
    }

    @Override
    public Class<String> getKeyType() {
        return String.class;
    }

    @Override
    public TransactionalOperation prepareInsert(E extension) {
        var primaryKey = extension.getMetadata().getName();
        var labels = extension.getMetadata().getLabels();
        return new UpsertTransactionalOperation(primaryKey, labels);
    }

    @Override
    public TransactionalOperation prepareUpdate(E extension) {
        var primaryKey = extension.getMetadata().getName();
        var labels = extension.getMetadata().getLabels();
        return new UpsertTransactionalOperation(primaryKey, labels);
    }

    @Override
    public TransactionalOperation prepareDelete(String primaryKey) {
        return new DeleteTransactionalOperation(primaryKey);
    }

    @Override
    public Set<String> exists(String labelKey) {
        return index.subMap(
                new LabelEntry(labelKey, null), true,
                new LabelEntry(labelKey, Character.MAX_VALUE + ""), true
            ).values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> equal(String labelKey, String labelValue) {
        return Optional.ofNullable(index.get(new LabelEntry(labelKey, labelValue)))
            .orElse(Set.of());
    }

    @Override
    public Set<String> notEqual(String labelKey, String labelValue) {
        // collect all primary keys
        var labelEntry = new LabelEntry(labelKey, labelValue);
        return index.subMap(
                new LabelEntry(labelKey, null), true,
                new LabelEntry(labelKey, Character.MAX_VALUE + ""), true
            )
            .entrySet().stream()
            .filter(entry -> !Objects.equals(entry.getKey(), labelEntry))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> in(String labelKey, Collection<String> labelValues) {
        if (CollectionUtils.isEmpty(labelValues)) {
            return Set.of();
        }
        return labelValues.stream()
            .distinct()
            .map(labelValue -> new LabelEntry(labelKey, labelValue))
            .map(index::get)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> notIn(String labelKey, Collection<String> labelValues) {
        if (CollectionUtils.isEmpty(labelValues)) {
            return Set.of();
        }
        var valueSet =
            labelValues instanceof Set<String> set ? set : Set.copyOf(labelValues);
        return index.subMap(
                new LabelEntry(labelKey, null), true,
                new LabelEntry(labelKey, Character.MAX_VALUE + ""), true
            )
            .entrySet()
            .stream()
            .filter(entry -> !valueSet.contains(entry.getKey().labelValue()))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    record LabelEntry(@NonNull String labelKey, @Nullable String labelValue)
        implements Comparable<LabelEntry> {

        public LabelEntry {
            Objects.requireNonNull(labelKey, "labelKey must not be null");
        }

        @Override
        public int compareTo(@NotNull LabelEntry o) {
            var compare = Comparator.<String>naturalOrder().compare(this.labelKey, o.labelKey);
            if (compare != 0) {
                return compare;
            }
            return Comparator.nullsFirst(Comparator.<String>naturalOrder())
                .compare(this.labelValue, o.labelValue);
        }
    }

    class UpsertTransactionalOperation implements TransactionalOperation {

        private final String primaryKey;

        private final Map<String, String> labels;

        private Map<String, String> previousLabels;

        private boolean committed;

        UpsertTransactionalOperation(String primaryKey, Map<String, String> labels) {
            this.primaryKey = primaryKey;
            this.labels = labels;
        }

        @Override
        public void prepare() {
            this.previousLabels = Optional.ofNullable(invertedIndex.get(primaryKey))
                .map(labelEntries -> labelEntries.stream()
                    .filter(entry -> entry.labelValue() != null)
                    .collect(toUnmodifiableMap(LabelEntry::labelKey, LabelEntry::labelValue))
                )
                .orElse(null);
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            this.committed = true;
            // remove old labels
            removeLabels(primaryKey, previousLabels);
            addLabels(primaryKey, labels);
        }

        @Override
        public void rollback() {
            if (!committed) {
                return;
            }
            removeLabels(primaryKey, labels);
            addLabels(primaryKey, previousLabels);
        }

    }

    class DeleteTransactionalOperation implements TransactionalOperation {

        private final String primaryKey;

        private Map<String, String> previousLabels;

        private boolean committed;

        DeleteTransactionalOperation(String primaryKey) {
            this.primaryKey = primaryKey;
        }

        @Override
        public void prepare() {
            this.previousLabels = Optional.ofNullable(invertedIndex.get(primaryKey))
                .map(labelEntries -> labelEntries.stream()
                    .filter(entry -> entry.labelValue() != null)
                    .collect(toUnmodifiableMap(LabelEntry::labelKey, LabelEntry::labelValue))
                )
                .orElse(null);
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            this.committed = true;
            removeLabels(primaryKey, previousLabels);
        }

        @Override
        public void rollback() {
            if (!committed) {
                return;
            }
            addLabels(primaryKey, previousLabels);
        }
    }


    private void removeLabels(String primaryKey, Map<String, String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            emptyLabelsSet.remove(primaryKey);
            return;
        }
        invertedIndex.remove(primaryKey);
        labels.forEach((labelKey, labelValue) -> {
            var labelEntry = new LabelEntry(labelKey, labelValue);
            index.computeIfPresent(labelEntry, (key, primaryKeys) -> {
                primaryKeys.remove(primaryKey);
                if (primaryKeys.isEmpty()) {
                    return null;
                }
                return primaryKeys;
            });
        });
    }

    private void addLabels(String primaryKey, Map<String, String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            emptyLabelsSet.add(primaryKey);
            return;
        }
        emptyLabelsSet.remove(primaryKey);
        labels.forEach((labelKey, labelValue) -> {
            var labelEntry = new LabelEntry(labelKey, labelValue);
            index.compute(labelEntry, (key, primaryKeys) -> {
                if (primaryKeys == null) {
                    primaryKeys = ConcurrentHashMap.newKeySet();
                }
                primaryKeys.add(primaryKey);
                return primaryKeys;
            });
            invertedIndex.compute(primaryKey, (pk, labelEntries) -> {
                if (labelEntries == null) {
                    labelEntries = ConcurrentHashMap.newKeySet();
                }
                labelEntries.add(labelEntry);
                return labelEntries;
            });
        });
    }
}
