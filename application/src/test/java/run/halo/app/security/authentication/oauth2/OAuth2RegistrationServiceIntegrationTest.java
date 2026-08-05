package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OAuth2RegistrationServiceIntegrationTest {

    @Autowired
    OAuth2RegistrationService registrationService;

    @Autowired
    ReactiveExtensionClient client;

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
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var oauth2User = new DefaultOAuth2User(List.of(), Map.of("sub", "oauth-no-email-registration"), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        StepVerifier.create(registrationService
                        .register(token, false)
                        .flatMap(result -> Mono.zip(
                                        client.get(User.class, result.username()),
                                        connectionService.getByProviderUserId("github", "oauth-no-email-registration"),
                                        roleService
                                                .getRolesByUsername(result.username())
                                                .collectList())
                                .doOnNext(persisted -> {
                                    var user = persisted.getT1();
                                    var connection = persisted.getT2();
                                    var roles = persisted.getT3();
                                    assertThat(result.needsEmailCompletion()).isTrue();
                                    assertThat(user.getSpec().getEmail()).isNull();
                                    assertThat(user.getSpec().getPassword()).isNull();
                                    assertThat(user.getSpec().isEmailVerified()).isFalse();
                                    assertThat(connection.getSpec().getUsername())
                                            .isEqualTo(result.username());
                                    assertThat(roles).contains("guest");
                                })
                                .thenReturn(result)))
                .assertNext(result -> assertThat(result.username()).isEqualTo("oauth-no-email-registration"))
                .verifyComplete();
    }
}
