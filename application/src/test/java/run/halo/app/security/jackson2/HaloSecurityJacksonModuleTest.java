package run.halo.app.security.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import run.halo.app.security.authentication.login.HaloUser;
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
        codecAssert(haloUser -> new TwoFactorAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(haloUser,
                haloUser.getPassword(),
                haloUser.getAuthorities())));
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
