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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.identity.entrypoint.JwtAccessDeniedHandler;
import run.halo.app.identity.entrypoint.JwtAuthenticationEntryPoint;

/**
 * @author guqing
 * @date 2022-04-12
 */
@WebMvcTest
@Import(TestWebSecurityConfig.class)
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
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
            .andExpect(status().is(401))
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

    @EnableWebSecurity
    @TestConfiguration
    static class TestWebSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests((authorize) -> authorize
                    .antMatchers("/api/**", "/apis/**").authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(
                    (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                    .accessDeniedHandler(new JwtAccessDeniedHandler())
                );
            return http.build();
        }
    }
}
