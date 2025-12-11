package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultIndicesManagerTest {

    @Mock
    SingleValueIndexSpec<Fake, String> singleValueIndexSpec;

    @Mock
    MultiValueIndexSpec<Fake, String> multiValueIndexSpec;

    @Mock
    SingleValueIndexSpec<Fake, Long> duplicateNameIndexSpec;

    @InjectMocks
    DefaultIndicesManager indicesManager;

    @BeforeEach
    void setUp() {
        lenient().when(singleValueIndexSpec.getName()).thenReturn("singleValueIndex");
        lenient().when(multiValueIndexSpec.getName()).thenReturn("multiValueIndex");
    }

    @Test
    void shouldAddDefaultIndexSpecs() {
        indicesManager.add(Fake.class, List.of(singleValueIndexSpec, multiValueIndexSpec));
        var indices = indicesManager.get(Fake.class);
        assertNotNull(indices.getIndex("metadata.name"));
        assertNotNull(indices.getIndex("metadata.creationTimestamp"));
        assertNotNull(indices.getIndex("metadata.deletionTimestamp"));
        assertNotNull(indices.getIndex("singleValueIndex"));
        assertNotNull(indices.getIndex("multiValueIndex"));
    }

    @Test
    void shouldThrowExceptionForUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> indicesManager.get(Fake.class));
    }

    @Test
    void shouldCloseIndicesManager() throws Exception {
        indicesManager.add(Fake.class, List.of(singleValueIndexSpec, multiValueIndexSpec));
        indicesManager.close();
        assertThrows(IllegalArgumentException.class, () -> indicesManager.get(Fake.class));
    }

    @Test
    void shouldNotOverwriteDefaultIndices() {
        when(duplicateNameIndexSpec.getName()).thenReturn("metadata.name");
        indicesManager.add(Fake.class, List.of(duplicateNameIndexSpec));
        var indices = indicesManager.get(Fake.class);
        var nameIndex = indices.getIndex("metadata.name");
        assertSame(String.class, nameIndex.getKeyType());
    }

    @Test
    void shouldRemoveIndices() {
        indicesManager.add(Fake.class, List.of(singleValueIndexSpec, multiValueIndexSpec));
        indicesManager.remove(Fake.class);
        assertThrows(IllegalArgumentException.class, () -> indicesManager.get(Fake.class));
    }

}
