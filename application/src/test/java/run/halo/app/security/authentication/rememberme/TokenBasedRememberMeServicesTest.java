package run.halo.app.security.authentication.rememberme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link TokenBasedRememberMeServices}.
 *
 * @author guqing
 * @since 2.16.0
 */
@ExtendWith(SpringExtension.class)
class TokenBasedRememberMeServicesTest {

    @Mock
    CookieSignatureKeyResolver cookieSignatureKeyResolver;

    @InjectMocks
    private TokenBasedRememberMeServices tokenBasedRememberMeServices;

    @Test
    void retrieveUserName() {
        var authentication = new TestingAuthenticationToken("fake-user", "test");
        var username = tokenBasedRememberMeServices.retrieveUserName(authentication);

        var userDetails = new User("zhangsan", "test", List.of());
        authentication = new TestingAuthenticationToken(userDetails, "test");
        username = tokenBasedRememberMeServices.retrieveUserName(authentication);
        assertThat(username).isEqualTo("zhangsan");
    }

    @Test
    void makeTokenSignatureTest() {
        when(cookieSignatureKeyResolver.resolveSigningKey()).thenReturn(Mono.just("fake-key"));
        var expireMs = 1716435187323L;
        tokenBasedRememberMeServices.makeTokenSignature(expireMs, "fake-user", "pwd-1",
                TokenBasedRememberMeServices.DEFAULT_ALGORITHM)
            .as(StepVerifier::create)
            .expectNext("29f1c7ccbb489741392d27ba5c30f30d05c79ee66289b6d6da5b431bba99a0c7")
            .verifyComplete();
    }

    @Test
    void encodeCookieTest() {
        var expireMs = 1716435187323L;
        var cookieTokens = new String[] {"fake-user", Long.toString(expireMs),
            TokenBasedRememberMeServices.DEFAULT_ALGORITHM,
            "29f1c7ccbb489741392d27ba5c30f30d05c79ee66289b6d6da5b431bba99a0c7"};
        var encode = tokenBasedRememberMeServices.encodeCookie(cookieTokens);
        assertThat(encode)
            .isEqualTo("ZmFrZS11c2VyOjE3MTY0MzUxODczMjM6U0hBLTI1NjoyOWYxYzdjY2JiNDg5NzQxMz"
                + "kyZDI3YmE1YzMwZjMwZDA1Yzc5ZWU2NjI4OWI2ZDZkYTViNDMxYmJhOTlhMGM3");
    }

    @Test
    void decodeCookieTest() {
        var cookieValue = "YWRtaW46MTcxODk2NDE3NDgwODpTSEE"
            + "tMjU2OmNkOTM0ZTAyZWQ4NGJmMzc1ZTA4MmE1OWU4YTA3NTNiMzA3ODg1MjZmYzA3Yjgy"
            + "YzVmY2Y3YmJiYzdjYzRkNWU";
        // 123 % 4 = 3, so we need to add 1 '=' to make it a multiple of 4 for
        // spring-security/gh-15127
        assertThat(cookieValue.length()).isEqualTo(123);
        var cookie = tokenBasedRememberMeServices.decodeCookie(cookieValue);
        assertThat(cookie).containsExactly("admin", "1718964174808", "SHA-256",
            "cd934e02ed84bf375e082a59e8a0753b30788526fc07b82c5fcf7bbbc7cc4d5e");

        cookieValue = "ZmFrZS11c2VyOjE3MTY0MzUxODczMjM6U0hBLTI1NjoyOWYxYzdjY2JiNDg5NzQxMz"
            + "kyZDI3YmE1YzMwZjMwZDA1Yzc5ZWU2NjI4OWI2ZDZkYTViNDMxYmJhOTlhMGM3";
        assertThat(cookieValue.length()).isEqualTo(128);
        cookie = tokenBasedRememberMeServices.decodeCookie(cookieValue);
        assertThat(cookie).containsExactly("fake-user", "1716435187323", "SHA-256",
            "29f1c7ccbb489741392d27ba5c30f30d05c79ee66289b6d6da5b431bba99a0c7");
    }
}
