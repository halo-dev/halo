package run.halo.app.core.endpoint.uc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.verification.SecurityVerificationService;

@ExtendWith(MockitoExtension.class)
class UcUserEndpointTest {

    WebTestClient webClient;

    @InjectMocks
    UcUserEndpoint endpoint;

    @Mock
    UserService userService;

    @Mock
    SecurityVerificationService securityVerificationService;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .webFilter((exchange, chain) -> chain.filter(exchange)
                        .onErrorResume(ResponseStatusException.class, error -> {
                            var response = exchange.getResponse();
                            response.setStatusCode(error.getStatusCode());
                            response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                            var problemDetail = error.getBody();
                            var bodyMap = new LinkedHashMap<String, Object>();
                            if (problemDetail.getProperties() != null) {
                                bodyMap.putAll(problemDetail.getProperties());
                            }
                            bodyMap.put("type", problemDetail.getType());
                            bodyMap.put("title", problemDetail.getTitle());
                            bodyMap.put("status", problemDetail.getStatus());
                            bodyMap.put("detail", problemDetail.getDetail());
                            var body = JsonUtils.objectToJson(bodyMap).getBytes(StandardCharsets.UTF_8);
                            return response.writeWith(
                                    Mono.just(response.bufferFactory().wrap(body)));
                        }))
                .apply(springSecurity())
                .build();
    }

    @Test
    void testGroupVersion() {
        var gv = endpoint.groupVersion();
        assertEquals("uc.api.halo.run", gv.group());
        assertEquals("v1alpha1", gv.version());
    }

    @Test
    void shouldNotGetCurrentUserWhenUnauthenticated() {
        webClient
                .mutate()
                .apply(mockAuthentication(new AnonymousAuthenticationToken(
                        "key", "anonymousUser", createAuthorityList("ROLE_ANONYMOUS"))))
                .build()
                .get()
                .uri("/users/-")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void shouldGetCurrentUserWhenPasswordSet() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .get()
                .uri("/users/-")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("faker")
                .jsonPath("$.displayName")
                .isEqualTo("Faker")
                .jsonPath("$.avatar")
                .isEqualTo("https://example.com/avatar.png")
                .jsonPath("$.passwordSet")
                .isEqualTo(true)
                .jsonPath("$.password")
                .doesNotExist();
    }

    @Test
    void shouldGetCurrentUserWhenPasswordNotSet() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(false)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .get()
                .uri("/users/-")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(false);
    }

    @Test
    void shouldSetPasswordWhenPasswordNotSet() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(false)));
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(true);

        verify(userService, never()).confirmPassword(anyString(), anyString());
        verify(userService, times(1)).updateWithRawPassword("faker", "new-password");
    }

    @Test
    void shouldIgnoreOldPasswordWhenPasswordNotSet() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(false)));
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "stale-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk();

        verify(userService, never()).confirmPassword(anyString(), anyString());
        verify(userService, times(1)).updateWithRawPassword("faker", "new-password");
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatched() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        when(userService.confirmPassword("faker", "old-password")).thenReturn(Mono.just(true));
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(true);

        verify(userService, times(1)).confirmPassword("faker", "old-password");
        verify(userService, times(1)).updateWithRawPassword("faker", "new-password");
    }

    @Test
    void shouldReturnPasswordSetWhenNewPasswordSameAsOld() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        when(userService.confirmPassword("faker", "old-password")).thenReturn(Mono.just(true));
        when(userService.updateWithRawPassword("faker", "same-password")).thenReturn(Mono.empty());
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "same-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(true);

        verify(userService, times(1)).updateWithRawPassword("faker", "same-password");
    }

    @Test
    void shouldNotChangePasswordWhenOldPasswordNotMatched() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        when(userService.confirmPassword("faker", "wrong-password")).thenReturn(Mono.just(false));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "wrong-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldNotChangePasswordWhenOldPasswordMissing() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).confirmPassword(anyString(), anyString());
        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldNotChangePasswordWhenOldPasswordBlank() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).confirmPassword(anyString(), anyString());
        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldNotSetPasswordWhenNewPasswordTooLong() {
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "%s"
                        }\
                        """.formatted("a".repeat(258)))
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldNotSetPasswordWhenPasswordPatternInvalid() {
        when(userService.getUser("faker")).thenReturn(Mono.just(createUser(false)));
        when(userService.updateWithRawPassword("faker", "new_password"))
                .thenReturn(Mono.error(new UnsatisfiedAttributeValueException("password does not match the pattern")));
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "new_password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, times(1)).updateWithRawPassword("faker", "new_password");
    }

    @Test
    void shouldNotSetPasswordWhenNewPasswordTooShort() {
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "1234"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateWithRawPassword(anyString(), anyString());
    }

    @Test
    void shouldNotSetPasswordWhenRequestBodyEmpty() {
        webClient
                .mutate()
                .apply(mockUser("faker"))
                .build()
                .put()
                .uri("/users/-/password")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldNotSetPasswordWhenUnauthenticated() {
        webClient
                .mutate()
                .apply(mockAuthentication(new AnonymousAuthenticationToken(
                        "key", "anonymousUser", createAuthorityList("ROLE_ANONYMOUS"))))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void shouldGetPasswordChangeVerificationRequired() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(true)));
        when(securityVerificationService.hasVerificationMethod(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(false);
        webClient
                .mutate()
                .apply(mockAuthentication(
                        new UsernamePasswordAuthenticationToken("faker", "password", createAuthorityList("ROLE_USER"))))
                .build()
                .get()
                .uri("/users/-")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordChangeVerificationRequired")
                .isEqualTo(true);
    }

    @Test
    void shouldNotGetPasswordChangeVerificationRequiredWhenVerified() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(true)));
        when(securityVerificationService.hasVerificationMethod(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(true);
        webClient
                .mutate()
                .apply(mockAuthentication(
                        new UsernamePasswordAuthenticationToken("faker", "password", createAuthorityList("ROLE_USER"))))
                .build()
                .get()
                .uri("/users/-")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordChangeVerificationRequired")
                .isEqualTo(false);
    }

    @Test
    void shouldForbidPasswordChangeWithoutVerification() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(true)));
        when(securityVerificationService.hasVerificationMethod(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(false);
        webClient
                .mutate()
                .apply(mockAuthentication(
                        new UsernamePasswordAuthenticationToken("faker", "password", createAuthorityList("ROLE_USER"))))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectBody()
                .jsonPath("$.redirectURI")
                .isEqualTo("/security-verification");
    }

    @Test
    void shouldChangePasswordWhenVerified() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(true)));
        when(securityVerificationService.hasVerificationMethod(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(true);
        when(userService.confirmPassword("faker", "old-password")).thenReturn(Mono.just(true));
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockAuthentication(
                        new UsernamePasswordAuthenticationToken("faker", "password", createAuthorityList("ROLE_USER"))))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(true);
    }

    @Test
    void shouldNotRequireVerificationWhenPasswordNotSet() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(false)));
        when(securityVerificationService.hasVerificationMethod(any())).thenReturn(true);
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockAuthentication(
                        new UsernamePasswordAuthenticationToken("faker", "password", createAuthorityList("ROLE_USER"))))
                .build()
                .put()
                .uri("/users/-/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk();
    }

    User userWithEmailVerified(boolean passwordSet) {
        var user = createUser(passwordSet);
        user.getSpec().setEmailVerified(true);
        return user;
    }

    User createUser(boolean passwordSet) {
        var spec = new User.UserSpec();
        spec.setDisplayName("Faker");
        spec.setEmail("faker@halo.run");
        spec.setAvatar("https://example.com/avatar.png");
        if (passwordSet) {
            spec.setPassword("fake-encoded-password");
        }
        var metadata = new Metadata();
        metadata.setName("faker");
        var user = new User();
        user.setSpec(spec);
        user.setMetadata(metadata);
        return user;
    }
}
