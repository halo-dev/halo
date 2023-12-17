package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.extension.GroupVersionKind.fromAPIVersionAndKind;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

import org.junit.jupiter.api.Test;

class RefTest {

    @Test
    void shouldHasSameGroupAndKind() {
        FakeExtension fake = new FakeExtension();
        Metadata metadata = new Metadata();
        metadata.setName("fake");
        fake.setMetadata(metadata);
        assertTrue(Ref.groupKindEquals(Ref.of(fake), fromExtension(fake.getClass())));
        // has different version
        assertTrue(Ref.groupKindEquals(Ref.of(fake),
            fromAPIVersionAndKind("fake.halo.run/v11111111111", "Fake")));
        assertFalse(Ref.groupKindEquals(Ref.of(fake),
            fromAPIVersionAndKind("fake.halo.run/v1alpha1", "NotFake")));
    }
}