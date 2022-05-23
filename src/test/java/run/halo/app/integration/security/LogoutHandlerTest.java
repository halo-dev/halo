package run.halo.app.integration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guqing
 * @since 2.0.0
 */
public class LogoutHandlerTest extends AuthorizationTestSuit {

    private MockMvc mockMvc;

    @Autowired
    SecurityFilterChain securityFilterChain;

    private String accessToken;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = setUpMock(SecuredController.class);
        OAuth2AccessTokenResponse tokenResponse = mockAuth();
        accessToken = tokenResponse.getAccessToken().getTokenValue();
    }

    @Test
    void allowAccessWithAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("Now you see me."));
    }

    @Test
    void cannotAccessAfterLogout() throws Exception {
        // logout
        mockMvc.perform(get("/logout")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().is2xxSuccessful());
        // access again.
        mockMvc.perform(get("/api/v1/posts")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @RestController
    @RequestMapping("/api/v1/posts")
    static class SecuredController {
        @GetMapping
        public String hello() {
            return "Now you see me.";
        }
    }
}
