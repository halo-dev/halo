package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.ExtensionStore;

class JSONExtensionConverterTest {

    JSONExtensionConverter converter;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        DefaultSchemeManager schemeManager = new DefaultSchemeManager(null);
        converter = new JSONExtensionConverter(schemeManager);
        objectMapper = JSONExtensionConverter.OBJECT_MAPPER;

        schemeManager.register(FakeExtension.class);
    }

    @Test
    void convertTo() throws IOException {
        var fake = createFakeExtension("fake", 10L);

        var extensionStore = converter.convertTo(fake);

        assertEquals("/registry/fake.halo.run/fakes/fake", extensionStore.getName());
        assertEquals(10L, extensionStore.getVersion());
        assertEquals(fake, objectMapper.readValue(extensionStore.getData(), FakeExtension.class));
    }

    @Test
    void convertFrom() throws JsonProcessingException {
        var fake = createFakeExtension("fake", 20L);

        var store = new ExtensionStore();
        store.setName("/registry/fake.halo.run/fakes/fake");
        store.setVersion(20L);
        store.setData(objectMapper.writeValueAsBytes(fake));

        FakeExtension gotFake = converter.convertFrom(FakeExtension.class, store);
        assertEquals(fake, gotFake);
    }

    @Test
    void shouldThrowConvertExceptionWhenDataIsInvalid() {
        var store = new ExtensionStore();
        store.setName("/registry/fake.halo.run/fakes/fake");
        store.setVersion(20L);
        store.setData("{".getBytes());

        assertThrows(ExtensionConvertException.class,
            () -> converter.convertFrom(FakeExtension.class, store));
    }

    @Test
    void shouldThrowSchemaViolationExceptionWhenNameNotSet() {
        var fake = new FakeExtension();
        Metadata metadata = new Metadata();
        fake.setMetadata(metadata);
        fake.setApiVersion("fake.halo.run/v1alpha1");
        fake.setKind("Fake");
        var error = assertThrows(SchemaViolationException.class, () -> converter.convertTo(fake));
        assertEquals(1, error.getErrors().size());
        assertEquals("$.metadata.name: null found, string expected",
            error.getErrors().iterator().next().getMessage());
    }

    FakeExtension createFakeExtension(String name, Long version) {
        var fake = new FakeExtension();
        fake.groupVersionKind(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"));
        Metadata metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(version);
        fake.setMetadata(metadata);

        return fake;
    }
}