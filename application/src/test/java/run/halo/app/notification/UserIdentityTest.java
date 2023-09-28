package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UserIdentity}.
 *
 * @author guqing
 * @since 2.9.0
 */
class UserIdentityTest {


    @Test
    void getEmailTest() {
        var identity = UserIdentity.anonymousWithEmail("test@example.com");
        assertThat(identity.getEmail().orElse(null)).isEqualTo("test@example.com");
    }
}