package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.exception.DuplicateNameException;

/**
 * Tests for {@link DefaultIndexer}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultIndexerTest {

    private static FakeExtension createFakeExtension() {
        var fake = new FakeExtension();
        fake.setMetadata(new Metadata());
        fake.getMetadata().setName("fake-extension");
        fake.setEmail("fake-email");
        return fake;
    }

    private static IndexSpec getNameIndexSpec() {
        return getIndexSpec("metadata.name", true,
            IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName()));
    }

    private static IndexSpec getIndexSpec(String name, boolean unique, IndexAttribute attribute) {
        return new IndexSpec()
            .setName(name)
            .setOrder(IndexSpec.OrderType.ASC)
            .setUnique(unique)
            .setIndexFunc(attribute);
    }

    @Test
    void constructor() {
        var spec = getNameIndexSpec();
        var descriptor = new IndexDescriptor(spec);
        descriptor.setReady(true);
        var indexContainer = new IndexEntryContainer();
        indexContainer.add(new IndexEntryImpl(descriptor));
        new DefaultIndexer(List.of(descriptor), indexContainer);
    }

    @Test
    void constructorWithException() {
        var spec = getNameIndexSpec();
        var descriptor = new IndexDescriptor(spec);
        var indexContainer = new IndexEntryContainer();
        assertThatThrownBy(() -> new DefaultIndexer(List.of(descriptor), indexContainer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Index descriptor is not ready for: metadata.name");
        descriptor.setReady(true);
        assertThatThrownBy(() -> new DefaultIndexer(List.of(descriptor), indexContainer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Index entry not found for: metadata.name");
    }

    @Test
    void getIndexEntryTest() {
        var spec = getNameIndexSpec();
        var descriptor = new IndexDescriptor(spec);
        descriptor.setReady(true);
        var indexContainer = new IndexEntryContainer();
        indexContainer.add(new IndexEntryImpl(descriptor));

        var defaultIndexer = new DefaultIndexer(List.of(descriptor), indexContainer);
        assertThatThrownBy(() -> defaultIndexer.getIndexEntry("not-exist"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No index found for fieldPath [not-exist], "
                + "make sure you have created an index for this field.");

        assertThat(defaultIndexer.getIndexEntry("metadata.name")).isNotNull();
    }

    @Test
    void getObjectKey() {
        var fake = createFakeExtension();
        assertThat(DefaultIndexer.getObjectKey(fake)).isEqualTo("fake-extension");
    }

    @Test
    void indexRecord() {
        var nameIndex = getNameIndexSpec();
        var indexContainer = new IndexEntryContainer();
        var descriptor = new IndexDescriptor(nameIndex);
        descriptor.setReady(true);
        indexContainer.add(new IndexEntryImpl(descriptor));

        var indexer = new DefaultIndexer(List.of(descriptor), indexContainer);
        indexer.indexRecord(createFakeExtension());

        var iterator = indexer.allIndexesIterator();
        assertThat(iterator.hasNext()).isTrue();
        var indexEntry = iterator.next();
        var entries = indexEntry.entries();
        assertThat(entries).hasSize(1);
        assertThat(entries).contains(Map.entry("fake-extension", "fake-extension"));
    }

    @Test
    void indexRecordWithExceptionShouldRollback() {
        var indexContainer = new IndexEntryContainer();
        // add email before name
        var emailDescriptor = new IndexDescriptor(getIndexSpec("email", false,
            IndexAttributeFactory.simpleAttribute(FakeExtension.class, FakeExtension::getEmail)));
        emailDescriptor.setReady(true);
        var emailIndexEntry = new IndexEntryImpl(emailDescriptor);
        indexContainer.add(emailIndexEntry);

        var descriptor = new IndexDescriptor(getNameIndexSpec());
        descriptor.setReady(true);
        var nameIndexEntry = new IndexEntryImpl(descriptor);
        indexContainer.add(nameIndexEntry);

        var indexer = new DefaultIndexer(List.of(descriptor, emailDescriptor), indexContainer);

        indexer.indexRecord(createFakeExtension());
        assertThat(emailIndexEntry.entries()).hasSize(1);
        assertThat(nameIndexEntry.entries()).hasSize(1);

        var fake2 = createFakeExtension();
        fake2.setEmail("email-2");

        // email applied to entry then name duplicate
        assertThatThrownBy(() -> indexer.indexRecord(fake2))
            .isInstanceOf(DuplicateNameException.class)
            .hasMessage(
                "400 BAD_REQUEST \"The value [fake-extension] is already exists for unique index "
                    + "[metadata.name].\"");

        // should be rollback email-2 key
        assertThat(emailIndexEntry.entries()).hasSize(1);
        assertThat(nameIndexEntry.entries()).hasSize(1);
    }

    @Test
    void updateRecordWithExceptionShouldRollback() {
        var indexContainer = new IndexEntryContainer();
        // add email before name
        var emailDescriptor = new IndexDescriptor(getIndexSpec("email", false,
            IndexAttributeFactory.simpleAttribute(FakeExtension.class, FakeExtension::getEmail)));
        emailDescriptor.setReady(true);
        var emailIndexEntry = new IndexEntryImpl(emailDescriptor);
        indexContainer.add(emailIndexEntry);

        var descriptor = new IndexDescriptor(getNameIndexSpec());
        descriptor.setReady(true);
        var nameIndexEntry = new IndexEntryImpl(descriptor);
        indexContainer.add(nameIndexEntry);

        var indexer = new DefaultIndexer(List.of(descriptor, emailDescriptor), indexContainer);

        var fakeExtension = createFakeExtension();
        indexer.indexRecord(fakeExtension);

        assertThat(emailIndexEntry.entries()).hasSize(1);
        assertThat(emailIndexEntry.entries()).contains(Map.entry("fake-email", "fake-extension"));
        assertThat(nameIndexEntry.entries()).hasSize(1);
        assertThat(nameIndexEntry.entries()).contains(
            Map.entry("fake-extension", "fake-extension"));

        fakeExtension.setEmail("email-2");
        indexer.updateRecord(fakeExtension);
        assertThat(emailIndexEntry.entries()).hasSize(1);
        assertThat(emailIndexEntry.entries()).contains(Map.entry("email-2", "fake-extension"));
        assertThat(nameIndexEntry.entries()).hasSize(1);
        assertThat(nameIndexEntry.entries()).contains(
            Map.entry("fake-extension", "fake-extension"));

        fakeExtension.getMetadata().setName("fake-extension-2");
        indexer.updateRecord(fakeExtension);
        assertThat(emailIndexEntry.entries())
            .containsExactly(Map.entry("email-2", "fake-extension"),
                Map.entry("email-2", "fake-extension-2"));
        assertThat(nameIndexEntry.entries())
            .containsExactly(Map.entry("fake-extension", "fake-extension"),
                Map.entry("fake-extension-2", "fake-extension-2"));
    }

    @Test
    void findIndexByName() {
        var indexContainer = new IndexEntryContainer();
        // add email before name
        var emailDescriptor = new IndexDescriptor(getIndexSpec("email", false,
            IndexAttributeFactory.simpleAttribute(FakeExtension.class, FakeExtension::getEmail)));
        emailDescriptor.setReady(true);
        var emailIndexEntry = new IndexEntryImpl(emailDescriptor);
        indexContainer.add(emailIndexEntry);

        var descriptor = new IndexDescriptor(getNameIndexSpec());
        descriptor.setReady(true);
        var nameIndexEntry = new IndexEntryImpl(descriptor);
        indexContainer.add(nameIndexEntry);

        var indexer = new DefaultIndexer(List.of(descriptor, emailDescriptor), indexContainer);

        var foundNameDescriptor = indexer.findIndexByName("metadata.name");
        assertThat(foundNameDescriptor).isNotNull();
        assertThat(foundNameDescriptor).isEqualTo(descriptor);

        var foundEmailDescriptor = indexer.findIndexByName("email");
        assertThat(foundEmailDescriptor).isNotNull();
        assertThat(foundEmailDescriptor).isEqualTo(emailDescriptor);
    }

    @Test
    void createIndexEntry() {
        var nameSpec = getNameIndexSpec();
        var descriptor = new IndexDescriptor(nameSpec);
        descriptor.setReady(true);
        var indexContainer = new IndexEntryContainer();
        indexContainer.add(new IndexEntryImpl(descriptor));
        var indexer = new DefaultIndexer(List.of(descriptor), indexContainer);
        var indexEntry = indexer.createIndexEntry(descriptor);
        assertThat(indexEntry).isNotNull();
    }

    @Test
    void removeIndexRecord() {
        var nameIndex = getNameIndexSpec();
        var indexContainer = new IndexEntryContainer();
        var descriptor = new IndexDescriptor(nameIndex);
        descriptor.setReady(true);
        var nameIndexEntry = new IndexEntryImpl(descriptor);
        indexContainer.add(nameIndexEntry);

        var indexer = new DefaultIndexer(List.of(descriptor), indexContainer);
        indexer.indexRecord(createFakeExtension());

        assertThat(nameIndexEntry.entries())
            .containsExactly(Map.entry("fake-extension", "fake-extension"));

        indexer.removeIndexRecords(d -> true);
        assertThat(nameIndexEntry.entries()).isEmpty();
    }

    @Test
    void readyIndexesIterator() {
        var indexContainer = new IndexEntryContainer();
        var descriptor = new IndexDescriptor(getNameIndexSpec());
        descriptor.setReady(true);
        var nameIndexEntry = new IndexEntryImpl(descriptor);
        indexContainer.add(nameIndexEntry);

        var indexer = new DefaultIndexer(List.of(descriptor), indexContainer);

        var iterator = indexer.readyIndexesIterator();
        assertThat(iterator.hasNext()).isTrue();

        descriptor.setReady(false);
        iterator = indexer.readyIndexesIterator();
        assertThat(iterator.hasNext()).isFalse();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "FakeExtension", plural = "fakeextensions",
        singular = "fakeextension")
    static class FakeExtension extends AbstractExtension {
        private String email;
    }
}
