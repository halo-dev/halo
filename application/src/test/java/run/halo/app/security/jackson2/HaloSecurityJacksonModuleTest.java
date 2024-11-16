package run.halo.app.security.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import run.halo.app.security.authentication.login.HaloUser;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

class HaloSecurityJacksonModuleTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = Jackson2ObjectMapperBuilder.json()
            .modules(SecurityJackson2Modules.getModules(this.getClass().getClassLoader()))
            .modules(modules -> modules.add(new HaloSecurityJackson2Module()))
            .indentOutput(true)
            .build();
    }

    @Test
    void codecHaloUserTest() throws JsonProcessingException {
        codecAssert(haloUser -> UsernamePasswordAuthenticationToken.authenticated(haloUser,
            haloUser.getPassword(),
            haloUser.getAuthorities()));
    }

    @Test
    void codecTwoFactorAuthenticationTokenTest() throws JsonProcessingException {
        codecAssert(haloUser -> {
            var authentication = UsernamePasswordAuthenticationToken.authenticated(haloUser,
                haloUser.getPassword(),
                haloUser.getAuthorities());
            return new TwoFactorAuthentication(authentication);
        });
    }

    @Test
    void codecHaloOAuth2AuthenticationTokenTest() throws JsonProcessingException {
        codecAssert(haloUser -> {
            var oauth2User = new DefaultOAuth2User(List.of(), Map.of("name", "halo"), "name");
            var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
            return new HaloOAuth2AuthenticationToken(haloUser, oauth2Token);
        });
    }

    void codecAssert(Function<HaloUser, Authentication> authenticationConverter)
        throws JsonProcessingException {
        var userDetails = User.withUsername("faker")
            .password("123456")
            .authorities("ROLE_USER")
            .build();
        var haloUser = new HaloUser(userDetails, true, "fake-encrypted-secret");

        var authentication = authenticationConverter.apply(haloUser);

        var securityContext = new SecurityContextImpl(authentication);
        var securityContextJson = objectMapper.writeValueAsString(securityContext);

        var deserializedSecurityContext =
            objectMapper.readValue(securityContextJson, SecurityContext.class);

        assertEquals(deserializedSecurityContext, securityContext);
    }
}
