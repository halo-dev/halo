package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.test.StepVerifier;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DefaultOAuth2RegistrationServiceIntegrationTest {

    @Autowired
    OAuth2RegistrationService registrationService;

    @Autowired
    UserService userService;

    @Autowired
    UserConnectionService connectionService;

    @Autowired
    RoleService roleService;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @Test
    void shouldPersistUserWithoutEmailDefaultRoleAndConnection() {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setMustVerifyEmailOnRegistration(true);
        setting.setDefaultRole("guest");
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(reactor.core.publisher.Mono.just(setting));
        var oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")), Map.of("sub", "oauth-no-email"), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        StepVerifier.create(registrationService.register(token, false))
                .assertNext(result -> {
                    assertThat(result.username()).isEqualTo("oauth-no-email");
                    assertThat(result.needsEmailCompletion()).isTrue();
                })
                .verifyComplete();

        StepVerifier.create(userService.getUser("oauth-no-email"))
                .assertNext(user -> {
                    assertThat(user.getSpec().getEmail()).isNull();
                    assertThat(user.getSpec().isEmailVerified()).isFalse();
                    assertThat(user.getSpec().getPassword()).isNull();
                })
                .verifyComplete();
        StepVerifier.create(connectionService.getByProviderUserId("github", "oauth-no-email"))
                .assertNext(connection ->
                        assertThat(connection.getSpec().getUsername()).isEqualTo("oauth-no-email"))
                .verifyComplete();
        StepVerifier.create(roleService.getRolesByUsername("oauth-no-email").collectList())
                .assertNext(roles -> assertThat(roles).contains("guest"))
                .verifyComplete();
    }
}
