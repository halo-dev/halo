package run.halo.app.extension.index;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;

public class IndexBuilderImpl implements IndexBuilder {
    private final List<IndexDescriptor> indexDescriptors;
    private final ExtensionIterator<? extends Extension> extensionIterator;

    private final IndexEntryContainer indexEntries = new IndexEntryContainer();

    public static IndexBuilder of(List<IndexDescriptor> indexDescriptors,
        ExtensionIterator<? extends Extension> extensionIterator) {
        return new IndexBuilderImpl(indexDescriptors, extensionIterator);
    }

    IndexBuilderImpl(List<IndexDescriptor> indexDescriptors,
        ExtensionIterator<? extends Extension> extensionIterator) {
        this.indexDescriptors = indexDescriptors;
        this.extensionIterator = extensionIterator;
        indexDescriptors.forEach(indexDescriptor -> {
            var indexEntry = new IndexEntryImpl(indexDescriptor);
            indexEntries.add(indexEntry);
        });
    }

    @Override
    public void startBuildingIndex() {
        while (extensionIterator.hasNext()) {
            var extensionRecord = extensionIterator.next();

            indexRecords(extensionRecord);
        }

        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            indexDescriptor.setReady(true);
        }
    }

    @Override
    @NonNull
    public IndexEntryContainer getIndexEntries() {
        for (IndexEntry indexEntry : indexEntries) {
            if (!indexEntry.getIndexDescriptor().isReady()) {
                throw new IllegalStateException(
                    "IndexEntry are not ready yet for index named "
                        + indexEntry.getIndexDescriptor().getSpec().getName());
            }
        }
        return indexEntries;
    }

    private <E extends Extension> void indexRecords(E extension) {
        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            var indexEntry = indexEntries.get(indexDescriptor);
            var indexFunc = indexDescriptor.getSpec().getIndexFunc();
            Set<String> indexKeys = indexFunc.getValues(extension);
            indexEntry.addEntry(new LinkedList<>(indexKeys),
                PrimaryKeySpecUtils.getObjectPrimaryKey(extension));
        }
    }
}
