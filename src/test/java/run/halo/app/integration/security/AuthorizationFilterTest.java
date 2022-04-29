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
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.identity.authorization.AuthorizationFilter;

/**
 * Tests for {@link AuthorizationFilter}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class AuthorizationFilterTest extends AuthorizationTestSuit {

    private MockMvc mockMvc;

    @Autowired
    SecurityFilterChain securityFilterChain;

    private String accessToken;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = setUpMock(PostController.class, TagController.class,
            HealthyController.class);
        OAuth2AccessTokenResponse tokenResponse = mockAuth();
        accessToken = tokenResponse.getAccessToken().getTokenValue();
    }

    @Test
    public void resourceRequestShouldOk() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("Now you see me."));
    }

    @Test
    public void resourceNameRequestShouldOk() throws Exception {
        mockMvc.perform(get("/api/v1/posts/halo")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("Name: halo, Now you see me."));
    }

    @Test
    public void resourceRequestWhenHaveNoRightShould403Status() throws Exception {
        mockMvc.perform(get("/api/v1/tags")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    public void resourceNameRequestWhenHaveNoRightShould403Status() throws Exception {
        mockMvc.perform(get("/api/v1/tags/tag-test")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }


    @Test
    public void resourceRequestWhenNotExistsThenShould404() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void nonResourceRequestWhenHaveRightShouldOk() throws Exception {
        mockMvc.perform(get("/healthy")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("ok."));
    }

    @Test
    public void nonResourceRequestWhenHaveNoRightShould403() throws Exception {
        mockMvc.perform(get("/healthy/halo")
                .headers(withBearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @RestController
    @RequestMapping("/api/v1/posts")
    public static class PostController {

        @GetMapping
        public String hello() {
            return "Now you see me.";
        }

        @GetMapping("/{name}")
        public String getByName(@PathVariable String name) {
            return "Name: " + name + ", Now you see me.";
        }
    }

    @RestController
    @RequestMapping("/api/v1/tags")
    public static class TagController {

        @GetMapping
        public String hello() {
            return "Tag you see me.";
        }

        @GetMapping("/{name}")
        public String getByName(@PathVariable String name) {
            return "Tag name:" + name + "-->Now you see me.";
        }
    }

    @Controller
    @RequestMapping("/healthy")
    public static class HealthyController {

        @GetMapping
        @ResponseBody
        public String check() {
            return "ok.";
        }

        @ResponseBody
        @GetMapping("/{name}")
        public String check(@PathVariable String name) {
            return name + ": should not be seen.";
        }
    }

}
