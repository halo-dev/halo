package run.halo.app.security.authentication.rememberme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link RememberTokenCleaner}.
 *
 * @author guqing
 * @since 2.17.0
 */
@ExtendWith(MockitoExtension.class)
class RememberTokenCleanerTest {
    @InjectMocks
    private RememberTokenCleaner rememberTokenCleaner;

    @Test
    void test() {
        var spyRememberTokenCleaner = spy(rememberTokenCleaner);
        Mockito.doReturn(Duration.ofSeconds(30)).when(spyRememberTokenCleaner).getTokenValidity();
        var expiredTime = spyRememberTokenCleaner.getExpirationThreshold();

        var creationTime = Instant.now().minus(Duration.ofSeconds(31));
        // creationTime < expirationThreshold means it has expired
        assertThat(creationTime).isBefore(expiredTime);

        // not expired
        creationTime = Instant.now().minus(Duration.ofSeconds(29));
        assertThat(creationTime).isAfter(expiredTime);
    }
}