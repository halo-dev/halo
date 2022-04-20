package run.halo.app.authentication.verifyer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationToken;

/**
 * Tests for {@link BearerTokenAuthenticationToken}
 *
 * @author guqing
 * @since 2.0.0
 */
public class BearerTokenAuthenticationTokenTest {
    @Test
    public void constructorWhenTokenIsNullThenThrowsException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenAuthenticationToken(null))
            .withMessageContaining("token cannot be empty");
    }

    @Test
    public void constructorWhenTokenIsEmptyThenThrowsException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenAuthenticationToken(""))
            .withMessageContaining("token cannot be empty");
    }

    @Test
    public void constructorWhenTokenHasValueThenConstructedCorrectly() {
        BearerTokenAuthenticationToken token = new BearerTokenAuthenticationToken("token");
        assertThat(token.getToken()).isEqualTo("token");
        assertThat(token.getPrincipal()).isEqualTo("token");
        assertThat(token.getCredentials()).isEqualTo("token");
    }
}
