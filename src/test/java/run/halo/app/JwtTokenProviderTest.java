package run.halo.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import run.halo.app.identity.authentication.AccessToken;
import run.halo.app.identity.authentication.JwtTokenProvider;

/**
 * @author guqing
 * @date 2022-04-13
 */
@WithMockUser(username = "test", password = "test")
@TestPropertySource(properties = {"halo.security.oauth2.jwt.public-key-location=classpath:app.pub",
    "halo.security.oauth2.jwt.private-key-location=classpath:app.key"})
@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private AccessToken accessToken;

    @BeforeEach
    public void setUp() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        accessToken = jwtTokenProvider.getToken(authentication);
    }

    @Test
    public void createJwt() throws MalformedURLException {
        Jwt token = accessToken.getAccessToken();
        Jwt refreshToken = accessToken.getRefreshToken();
        System.out.println(token.getTokenValue());
        assertThat(token.getClaims()).isNotNull()
            .containsEntry("sub", "test")
            .containsEntry("scope", "ROLE_USER")
            .containsEntry("iss", new URL("https://halo.run"))
            .containsKey("iss")
            .containsKey("jti");

        assertThat(refreshToken.getClaims())
            .isNotNull()
            .containsEntry("sub", "test")
            .containsKey("iss")
            .containsKey("jti");
    }

    @Test
    public void verifyBadToken() {
        Jwt badToken = jwtTokenProvider.verify("badToken");
        assertThat(badToken).isNull();
    }
}
