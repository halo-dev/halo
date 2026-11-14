package run.halo.app.security.authentication.emailcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EmailCodeAuthenticationToken}.
 *
 * @author johnniang
 * @since 2.26.0
 */
class EmailCodeAuthenticationTokenTest {

    @Test
    void shouldCreateUnauthenticatedToken() {
        var token = new EmailCodeAuthenticationToken("test@example.com", "123456");

        assertThat(token.isAuthenticated()).isFalse();
        assertThat(token.getPrincipal()).isEqualTo("test@example.com");
        assertThat(token.getCredentials()).isEqualTo("123456");
        assertThat(token.getName()).isEqualTo("test@example.com");
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> new EmailCodeAuthenticationToken("", "123456"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EmailCodeAuthenticationToken(null, "123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEraseCredentials() {
        var token = new EmailCodeAuthenticationToken("test@example.com", "123456");
        token.eraseCredentials();

        assertThat(token.getCredentials()).isNull();
    }
}
