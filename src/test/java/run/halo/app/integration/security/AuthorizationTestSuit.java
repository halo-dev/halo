package run.halo.app.integration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0.0
 */
@WebMvcTest
@Import(TestWebSecurityConfig.class)
public class AuthorizationTestSuit {
    private MockMvc mockMvc;
    @Autowired
    SecurityFilterChain securityFilterChain;
    @Autowired
    ObjectMapper objectMapper;

    public MockMvc setUpMock(Object... controllers) {
        mockMvc = MockMvcBuilders.standaloneSetup(controllers)
            .addFilters(securityFilterChain.getFilters().toArray(new Filter[] {}))
            .build();
        return mockMvc;
    }

    public OAuth2AccessTokenResponse mockAuth() throws Exception {
        Assert.notNull(mockMvc, "call setUpMock(args) method first.");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/oauth2/token")
                .param("username", "test_user")
                .param("password", "123456")
                .param("grant_type", "password"))
            .andExpect(status().is2xxSuccessful())
            .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> stringStringMap =
            objectMapper.readValue(content, new TypeReference<>() {});
        DefaultMapOAuth2AccessTokenResponseConverter converter =
            new DefaultMapOAuth2AccessTokenResponseConverter();
        return converter.convert(stringStringMap);
    }

    public HttpHeaders withBearerToken(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return httpHeaders;
    }
}
