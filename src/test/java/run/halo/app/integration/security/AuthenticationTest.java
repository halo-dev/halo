package run.halo.app.integration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.config.WebSecurityConfig;

/**
 * @author guqing
 * @date 2022-04-12
 */
@TestPropertySource(properties = {"halo.security.oauth2.jwt.public-key-location=classpath:app.pub",
    "halo.security.oauth2.jwt.private-key-location=classpath:app.key"})
@WebMvcTest
@Import(WebSecurityConfig.class)
public class AuthenticationTest {

    private MockMvc mockMvc;

    @Autowired
    SecurityFilterChain securityFilterChain;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(AnonymousAllowedController.class,
                SecuredController.class)
            .addFilters(securityFilterChain.getFilters().toArray(new Filter[] {}))
            .build();
    }

    @Test
    public void allowAccessTest() throws Exception {
        mockMvc.perform(get("/anonymous"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("Hello, now you see me."));
    }

    @Test
    public void securedApiTest() throws Exception {
        mockMvc.perform(get("/api/secured"))
            .andDo(print())
            .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(content().string("Unauthorized"));
    }

    @Controller
    @RequestMapping("/anonymous")
    static class AnonymousAllowedController {
        @GetMapping
        @ResponseBody
        public String hello() {
            return "Hello, now you see me.";
        }
    }

    @RestController
    @RequestMapping("/api/secured")
    static class SecuredController {
        @GetMapping
        public String hello() {
            return "You can't see me.";
        }
    }
}
