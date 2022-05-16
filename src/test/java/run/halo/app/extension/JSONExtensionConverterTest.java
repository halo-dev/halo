package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.store.ExtensionStore;

@ExtendWith(MockitoExtension.class)
class JSONExtensionConverterTest {

    JSONExtensionConverter converter;

    ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() {
        Schemes.INSTANCE.register(FakeExtension.class);
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        converter = new JSONExtensionConverter(objectMapper);
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
    void shouldThrowExceptionWhenDataIsInvalid() {
        var store = new ExtensionStore();
        store.setName("/registry/fake.halo.run/fakes/fake");
        store.setVersion(20L);
        store.setData("{".getBytes());

        assertThrows(ExtensionConvertException.class,
            () -> converter.convertFrom(FakeExtension.class, store));
    }


    FakeExtension createFakeExtension(String name, Long version) {
        var fake = new FakeExtension();
        fake.groupVersionKind(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"));
        Metadata metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(version);
        fake.metadata(metadata);

        return fake;
    }
}