package run.halo.app.security.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import run.halo.app.security.authentication.login.HaloUser;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

class HaloSecurityJacksonModuleTest {

    JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        var haloSecurityModule = new HaloSecurityJacksonModule();
        var typeValidatorBuilder = BasicPolymorphicTypeValidator.builder();
        haloSecurityModule.configurePolymorphicTypeValidator(typeValidatorBuilder);
        this.objectMapper = JsonMapper.builder()
            .addModules(SecurityJacksonModules.getModules(
                this.getClass().getClassLoader(), typeValidatorBuilder
            ))
            .addModules(haloSecurityModule)
            .build();
    }

    @Test
    void codecHaloUserTest() {
        codecAssert(haloUser -> UsernamePasswordAuthenticationToken.authenticated(haloUser,
            haloUser.getPassword(),
            haloUser.getAuthorities()));
    }

    @Test
    void codecTwoFactorAuthenticationTokenTest() {
        codecAssert(haloUser -> {
            var authentication = UsernamePasswordAuthenticationToken.authenticated(haloUser,
                haloUser.getPassword(),
                haloUser.getAuthorities());
            return new TwoFactorAuthentication(authentication);
        });
    }

    @Test
    void codecHaloOAuth2AuthenticationTokenTest() {
        codecAssert(haloUser -> {
            var oauth2User = new DefaultOAuth2User(List.of(), Map.of("name", "halo"), "name");
            var oauth2Token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
            return new HaloOAuth2AuthenticationToken(haloUser, oauth2Token);
        });
    }

    @Test
    void shouldReadSwitchUserGrantedAuthority() {
        codecAssert(haloUser -> {
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                haloUser.getUsername(), haloUser.getPassword(), haloUser.getAuthorities()
            );
            var switchUserGrantedAuthority =
                new SwitchUserGrantedAuthority("ADMIN", authentication);
            var extendedAuthorities = new ArrayList<>(authentication.getAuthorities());
            extendedAuthorities.add(switchUserGrantedAuthority);
            authentication = UsernamePasswordAuthenticationToken.authenticated(
                authentication.getPrincipal(), authentication.getCredentials(), extendedAuthorities
            );
            return authentication;
        });
    }

    void codecAssert(Function<HaloUser, Authentication> authenticationConverter) {
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
