package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.ExtensionStore;

@ExtendWith(MockitoExtension.class)
class JsonExtensionConverterTest {

    @InjectMocks
    JSONExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    ObjectMapper objectMapper = Unstructured.OBJECT_MAPPER;

    @BeforeEach
    void setUp() {
        converter.setObjectMapper(objectMapper);

        var scheme = Scheme.buildFromType(FakeExtension.class);
        lenient().when(schemeManager.get(scheme.groupVersionKind())).thenReturn(scheme);
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

        assertThrows(ExtensionConvertException.class, () -> converter.convertFrom(FakeExtension.class, store));
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
        var result = error.getErrors().get(0);
        assertEquals("required", result.getKeyword());
        assertEquals("/metadata", result.getInstanceLocation().toString());
        assertTrue(result.getMessage().contains("name"));
    }

    @Test
    void shouldAllowNullValueForNullableField() {
        var scheme = Scheme.buildFromType(NullableExtension.class);
        lenient().when(schemeManager.get(scheme.groupVersionKind())).thenReturn(scheme);

        var extension = new NullableExtension();
        extension.setApiVersion("fake.halo.run/v1alpha1");
        extension.setKind("Nullable");
        var metadata = new Metadata();
        metadata.setName("nullable");
        metadata.setGenerateName("nullable");
        metadata.setLabels(Map.of());
        metadata.setAnnotations(Map.of());
        extension.setMetadata(metadata);
        extension.setOptional(null);

        // Ensure the null field is serialized instead of omitted, so that the validator has to
        // accept it according to the nullable keyword.
        converter.setObjectMapper(
                Unstructured.OBJECT_MAPPER.copy().setSerializationInclusion(JsonInclude.Include.ALWAYS));

        var store = assertDoesNotThrow(() -> converter.convertTo(extension));
        assertTrue(new String(store.getData(), StandardCharsets.UTF_8).contains("\"optional\":null"));
    }

    @GVK(group = "fake.halo.run", version = "v1alpha1", kind = "Nullable", plural = "nullables", singular = "nullable")
    static class NullableExtension extends AbstractExtension {

        private String optional;

        @Schema(nullable = true)
        public String getOptional() {
            return optional;
        }

        public void setOptional(String optional) {
            this.optional = optional;
        }
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
