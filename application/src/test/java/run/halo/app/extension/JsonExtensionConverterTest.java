package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.Extensions;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class JsonExtensionConverterTest {

    @InjectMocks
    JSONExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    JsonMapper objectMapper = Unstructured.jsonMapper();

    @BeforeEach
    void setUp() {
        converter.setJsonMapper(objectMapper);

        var scheme = Scheme.buildFromType(FakeExtension.class);
        lenient().when(schemeManager.get(scheme.groupVersionKind())).thenReturn(scheme);
    }

    @Test
    void convertToFromUnstructured() {
        var scheme = Scheme.buildFromType(Role.class);
        when(schemeManager.get(scheme.groupVersionKind())).thenReturn(scheme);
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("super-role");
        role.getMetadata().setVersion(10L);
        role.setRules(new ArrayList<>());
        role.getRules().add(new Role.PolicyRule(
            new String[] {"fake.api.halo.run"},
            new String[] {"fakes"},
            new String[] {"fake"},
            new String[] {},
            new String[] {"get"}
        ));

        var unstructured = Unstructured.jsonMapper().convertValue(role, Unstructured.class);

        var extensionStore = converter.convertTo(unstructured);
        assertEquals("/registry/roles/super-role", extensionStore.getName());
        assertEquals(10L, extensionStore.getVersion());
        assertEquals(role, objectMapper.readValue(extensionStore.getData(), Role.class));
    }

    @Test
    void convertTo() {
        var fake = createFakeExtension("fake", 10L);
        fake.setStatus(new FakeExtension.FakeStatus());
        fake.getStatus().setState("running");

        var extensionStore = converter.convertTo(fake);

        assertEquals("/registry/fake.halo.run/fakes/fake", extensionStore.getName());
        assertEquals(10L, extensionStore.getVersion());
        assertEquals(fake, objectMapper.readValue(extensionStore.getData(), FakeExtension.class));
    }

    @Test
    void convertFrom() {
        var fake = createFakeExtension("fake", 20L);

        var store = new Extensions();
        store.setName("/registry/fake.halo.run/fakes/fake");
        store.setVersion(20L);
        store.setData(objectMapper.writeValueAsBytes(fake));

        FakeExtension gotFake = converter.convertFrom(FakeExtension.class, store);
        assertEquals(fake, gotFake);
    }

    @Test
    void shouldThrowConvertExceptionWhenDataIsInvalid() {
        var store = new Extensions();
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
        var result = error.getErrors().items().get(0);
        assertEquals(1026, result.code());
        assertEquals("Field 'name' is required.", result.message());
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