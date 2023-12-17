package run.halo.app.core.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Metadata;

class RoleBindingTest {

    @Test
    void shouldContainUser() {
        var subject = new RoleBinding.Subject();
        subject.setName("fake-name");
        subject.setApiGroup("");
        subject.setKind("User");

        var binding = new RoleBinding();
        binding.setMetadata(new Metadata());
        binding.setSubjects(List.of(subject));
        assertTrue(RoleBinding.containsUser("fake-name").test(binding));
        assertFalse(RoleBinding.containsUser("non-exist-fake-name").test(binding));
    }

    @Test
    void shouldNotContainUserWhenBindingIsDeleted() {
        var subject = new RoleBinding.Subject();
        subject.setName("fake-name");
        subject.setApiGroup("");
        subject.setKind("User");

        var binding = new RoleBinding();
        var metadata = new Metadata();
        metadata.setDeletionTimestamp(Instant.now());
        binding.setMetadata(metadata);
        binding.setSubjects(List.of(subject));
        assertFalse(RoleBinding.containsUser("fake-name").test(binding));
        assertFalse(RoleBinding.containsUser("non-exist-fake-name").test(binding));
    }

}