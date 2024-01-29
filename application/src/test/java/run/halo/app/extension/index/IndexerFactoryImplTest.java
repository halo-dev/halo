package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;

/**
 * Tests for {@link IndexerFactoryImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class IndexerFactoryImplTest {
    @Mock
    private SchemeManager schemeManager;
    @Mock
    private IndexSpecRegistry indexSpecRegistry;

    @InjectMocks
    IndexerFactoryImpl indexerFactory;

    @Test
    @SuppressWarnings("unchecked")
    void indexFactory() {
        var scheme = Scheme.buildFromType(DemoExtension.class);
        when(schemeManager.get(eq(DemoExtension.class)))
            .thenReturn(scheme);
        when(indexSpecRegistry.getKeySpace(scheme))
            .thenReturn("/registry/test/demoextensions");
        when(indexSpecRegistry.contains(eq(scheme)))
            .thenReturn(false);
        var specs = mock(IndexSpecs.class);
        when(indexSpecRegistry.getIndexSpecs(eq(scheme)))
            .thenReturn(specs);
        when(specs.getIndexSpecs())
            .thenReturn(List.of(PrimaryKeySpecUtils.primaryKeyIndexSpec(DemoExtension.class)));
        ExtensionIterator<DemoExtension> iterator = mock(ExtensionIterator.class);
        when(iterator.hasNext()).thenReturn(false);
        // create indexer
        var indexer = indexerFactory.createIndexerFor(DemoExtension.class, iterator);
        assertThat(indexer).isNotNull();

        when(schemeManager.fetch(eq(scheme.groupVersionKind()))).thenReturn(Optional.of(scheme));
        when(schemeManager.get(eq(scheme.groupVersionKind()))).thenReturn(scheme);
        // contains indexer
        var hasIndexer = indexerFactory.contains(scheme.groupVersionKind());
        assertThat(hasIndexer).isTrue();

        assertThat(indexerFactory.contains(
            new GroupVersionKind("test", "v1", "Post"))).isFalse();

        // get indexer
        var foundIndexer = indexerFactory.getIndexer(scheme.groupVersionKind());
        assertThat(foundIndexer).isEqualTo(indexer);

        // remove indexer
        indexerFactory.removeIndexer(scheme);
        assertThat(indexerFactory.contains(scheme.groupVersionKind())).isFalse();

        // verify
        verify(indexSpecRegistry).indexFor(eq(scheme));
        verify(schemeManager).get(eq(DemoExtension.class));
        verify(indexSpecRegistry, times(4)).getKeySpace(eq(scheme));
        verify(indexSpecRegistry).contains(eq(scheme));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "DemoExtension", plural = "demoextensions",
        singular = "demoextension")
    static class DemoExtension extends AbstractExtension {
        private String email;
    }
}
