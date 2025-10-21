package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.FakeExtension;

class SingleValueBuilderTest {

    @Test
    void throwIfNoNameProvided() {
        assertThrows(IllegalArgumentException.class,
            () -> new SingleValueBuilder<FakeExtension, String>(null, String.class)
        );
    }

    @Test
    void throwIfNoKeyTypeProvided() {
        assertThrows(IllegalArgumentException.class, () ->
            new SingleValueBuilder<FakeExtension, String>("metadata.name", null)
        );
    }

    @Test
    void throwIfNoIndexFuncProvided() {
        var builder = new SingleValueBuilder<FakeExtension, String>("metadata.name", String.class);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldBuildCorrectly() {
        var builder = new SingleValueBuilder<FakeExtension, String>("metadata.name", String.class)
            .indexFunc(e -> e.getMetadata().getName());
        var indexSpec = builder.build();
        assertNotNull(indexSpec);
        assertInstanceOf(SingleValueIndexSpec.class, indexSpec);
        assertEquals(String.class, indexSpec.getKeyType());
        assertEquals("metadata.name", indexSpec.getName());
        assertFalse(indexSpec.isUnique());
        assertTrue(indexSpec.isNullable());
    }
}