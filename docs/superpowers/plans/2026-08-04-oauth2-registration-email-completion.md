# OAuth2 登录注册与邮箱补充门禁实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** OAuth2 未绑定用户可选择绑定已有账号或一键注册；注册无可用邮箱/未验证邮箱的用户被门禁引导到 `/complete-profile` 补邮箱（按“注册需验证邮箱”设置决定是否必须验证码），超级管理员豁免。

**Architecture:** 网关服务端新增选择页与 `/complete-profile` 页面；新增 `OAuth2RegistrationService` 负责注册核心逻辑；新增独立 WebFilter 门禁；复用现有 `EmailVerificationService`、`HaloServerRequestCache`、`LoginHandlerEnhancer`。

**Tech Stack:** Java 21、Spring Boot WebFlux + Security、Thymeleaf 网关模板、Reactor、JUnit 5 + Mockito + WebTestClient。

---

## 文件结构总览

**新增文件**

|                                                        文件                                                         |                            职责                             |
|-------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `api/src/main/java/run/halo/app/core/extension/User.java`（修改）                                                     | `spec.email` 改为可选                                         |
| `api/src/main/java/run/halo/app/core/user/service/UserService.java`（修改）                                           | 新增 `checkEmailInUse(username, email)`                     |
| `application/src/main/java/run/halo/app/core/user/service/impl/UserServiceImpl.java`（修改）                          | 实现 `checkEmailInUse`                                      |
| `application/src/main/java/run/halo/app/security/authentication/oauth2/OAuth2RegistrationService.java`（新增）        | 注册服务接口 + `RegistrationResult`                             |
| `application/src/main/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationService.java`（新增） | 注册核心逻辑                                                    |
| `application/src/main/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpoint.java`（新增）              | `/login?oauth2_select` 渲染 + `POST /login/oauth2/register` |
| `application/src/main/java/run/halo/app/security/completion/EmailCompletionFilter.java`（新增）                       | 门禁 WebFilter                                              |
| `application/src/main/java/run/halo/app/security/completion/EmailCompletionSecurityConfigurer.java`（新增）           | 注册门禁过滤器到安全链                                               |
| `application/src/main/java/run/halo/app/security/completion/EmailCompletionEndpoint.java`（新增）                     | `/complete-profile` 页面与接口                                 |
| `application/src/main/resources/templates/login_oauth2_select.html`（新增）                                           | 选择页根模板                                                    |
| `application/src/main/resources/templates/gateway_fragments/oauth2_select.html`（新增）                               | 选择页表单片段                                                   |
| `application/src/main/resources/templates/complete_profile.html`（新增）                                              | 补邮箱页根模板                                                   |
| `application/src/main/resources/templates/gateway_fragments/complete_profile.html`（新增）                            | 补邮箱表单片段                                                   |
| 4 个 `login_oauth2_select*.properties` + 4 个 `complete_profile*.properties`（新增）                                    | 页面文案                                                      |
| 对应测试文件（见各任务）                                                                                                      | 测试                                                        |

**修改文件**

|                                                     文件                                                     |                   修改点                    |
|------------------------------------------------------------------------------------------------------------|------------------------------------------|
| `application/src/main/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilter.java` | 未绑定重定向改为 `/login?oauth2_select`          |
| `application/src/main/java/run/halo/app/infra/config/WebServerSecurityConfig.java`                         | 新增 `OAuth2AuthenticationTokenCache` Bean |
| `application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java`      | `/complete-profile/**` 要求已认证             |

---

## Task 1: User.email 改为可选

**Files:**
- Modify: `api/src/main/java/run/halo/app/core/extension/User.java:64-66`
- Test: `api/src/test/java/run/halo/app/core/extension/UserSchemaTest.java`

- [ ] **Step 1: 写失败测试**

创建 `api/src/test/java/run/halo/app/core/extension/UserSchemaTest.java`：

```java
package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Scheme;

class UserSchemaTest {

    @Test
    void shouldNotRequireEmailInUserSpec() {
        var scheme = Scheme.buildFromType(User.class);
        var userSpec = scheme.openApiSchema()
                .path("components")
                .path("schemas")
                .path("UserSpec");
        var required = new ArrayList<String>();
        userSpec.path("required").forEach(node -> required.add(node.asText()));
        assertThat(required).doesNotContain("email");
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :api:test --tests "run.halo.app.core.extension.UserSchemaTest"`
Expected: FAIL，`required` 中包含 `email`。

- [ ] **Step 3: 实现**

修改 `api/src/main/java/run/halo/app/core/extension/User.java`，删除 `spec.email` 上的 `@Schema(requiredMode = REQUIRED)`：

```java
/** Email address used for sign-in and notifications. */
private String email;
```

保留 `displayName` 的 `@Schema(requiredMode = REQUIRED)` 不变。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :api:test --tests "run.halo.app.core.extension.UserSchemaTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add api/src/main/java/run/halo/app/core/extension/User.java api/src/test/java/run/halo/app/core/extension/UserSchemaTest.java
git commit -m "feat: make user email optional in schema for OAuth2 registration"
```

---

## Task 2: OAuth2RegistrationService 核心注册逻辑

**Files:**
- Create: `application/src/main/java/run/halo/app/security/authentication/oauth2/OAuth2RegistrationService.java`
- Create: `application/src/main/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationService.java`
- Test: `application/src/test/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationServiceTest.java`

- [ ] **Step 1: 写失败测试**

创建 `application/src/test/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationServiceTest.java`：

```java
package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AgreementNotAcceptedException;

@ExtendWith(MockitoExtension.class)
class DefaultOAuth2RegistrationServiceTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @Mock
    UserConnectionService connectionService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    jakarta.validation.Validator validator;

    @Mock
    Clock clock;

    DefaultOAuth2RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultOAuth2RegistrationService(
                client, userService, connectionService, systemConfigFetcher, validator, clock);
        when(clock.instant()).thenReturn(Instant.parse("2026-08-04T00:00:00Z"));
        when(validator.validate(any())).thenReturn(Set.of());
    }

    SystemSetting.User userSetting() {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setDefaultRole("author");
        return setting;
    }

    OAuth2AuthenticationToken token(String name, Map<String, Object> attributes) {
        var user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                attributes, "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "github");
    }

    @Test
    void shouldRegisterWithOAuth2NameAndVerifiedEmail() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(false));

        var created = new User();
        created.setMetadata(new Metadata());
        created.getMetadata().setName("alice");
        when(userService.createUser(any(User.class), eq(Set.of("author")))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, User.class);
            created.setSpec(user.getSpec());
            return Mono.just(created);
        });
        when(userService.getUser("alice")).thenReturn(Mono.just(created));
        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.just(connection));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com", "name", "Alice"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        verify(userService).createUser(any(User.class), eq(Set.of("author")));
        assertThat(created.getSpec().getEmail()).isEqualTo("alice@example.com");
        assertThat(created.getSpec().isEmailVerified()).isTrue();
        assertThat(created.getSpec().getDisplayName()).isEqualTo("Alice");
        assertThat(created.getSpec().getRegisteredAt()).isEqualTo(Instant.parse("2026-08-04T00:00:00Z"));
    }

    @Test
    void shouldUseRandomUsernameWhenCandidateIsTaken() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.just(new User()));
        when(client.fetch(eq(User.class), org.mockito.ArgumentMatchers.argThat(name -> !"alice".equals(name))))
                .thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUser(anyString())).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(invocation.getArgument(0));
            var userSpec = new User.UserSpec();
            userSpec.setEmail("alice@example.com");
            userSpec.setEmailVerified(true);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldLeaveEmailBlankWhenEmailIsTaken() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(true));
        when(userService.createUser(any(User.class), anySet())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUser(anyString())).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(invocation.getArgument(0));
            var userSpec = new User.UserSpec();
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getSpec().getEmail()).isNull();
        assertThat(captor.getValue().getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldKeepEmailUnverifiedForOidcUserWhenEmailVerifiedIsFalse() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "sub-123")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "sub-123")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("user@example.com")).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUser("sub-123")).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName("sub-123");
            var userSpec = new User.UserSpec();
            userSpec.setEmail("user@example.com");
            userSpec.setEmailVerified(false);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("sub-123");
        when(oidcUser.getClaimAsString("email")).thenReturn("user@example.com");
        when(oidcUser.getClaimAsBoolean("email_verified")).thenReturn(false);
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("sub-123"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getSpec().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldRejectWhenRegistrationDisabled() {
        var setting = userSetting();
        setting.setAllowRegistration(false);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .expectError(ServerWebInputException.class)
                .verify();
        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRejectWhenAgreementNotAccepted() {
        var setting = userSetting();
        setting.setRequiredAgreementPages(List.of("page"));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .expectError(AgreementNotAcceptedException.class)
                .verify();
    }

    @Test
    void shouldReturnExistingUsernameWhenConnectionExists() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername("alice");
        connection.setSpec(spec);
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.just(connection));
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        var userSpec = new User.UserSpec();
        userSpec.setEmail("alice@example.com");
        userSpec.setEmailVerified(true);
        user.setSpec(userSpec);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();
        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldDeleteUserWhenConnectionCreationFails() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        var created = new User();
        created.setMetadata(new Metadata());
        created.getMetadata().setName("alice");
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(created));
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.error(new IllegalStateException("already bound")));
        when(client.delete(created)).thenReturn(Mono.just(created));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .expectError(IllegalStateException.class)
                .verify();
        verify(client).delete(created);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.oauth2.DefaultOAuth2RegistrationServiceTest"`
Expected: FAIL，编译错误（`OAuth2RegistrationService` / `DefaultOAuth2RegistrationService` 不存在）。

- [ ] **Step 3: 创建接口与实现**

创建 `application/src/main/java/run/halo/app/security/authentication/oauth2/OAuth2RegistrationService.java`：

```java
package run.halo.app.security.authentication.oauth2;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Service for registering a Halo user from an unbound OAuth2 identity.
 */
public interface OAuth2RegistrationService {

    /**
     * Register a new user from the given OAuth2 token.
     *
     * @param token cached OAuth2 authentication token
     * @param agreedToTerms whether the user agreed to required agreement pages
     * @return registration result containing the created (or already bound) username
     */
    Mono<RegistrationResult> register(OAuth2AuthenticationToken token, boolean agreedToTerms);

    record RegistrationResult(String username, boolean needsEmailCompletion) {}
}
```

创建 `application/src/main/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationService.java`：

```java
package run.halo.app.security.authentication.oauth2;

import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.AgreementNotAcceptedException;

/**
 * Default implementation of {@link OAuth2RegistrationService}.
 */
@Service
@RequiredArgsConstructor
public class DefaultOAuth2RegistrationService implements OAuth2RegistrationService {

    private static final int RANDOM_USERNAME_MAX_ATTEMPTS = 20;
    private static final String RANDOM_USERNAME_PREFIX = "user-";

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final UserConnectionService connectionService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final jakarta.validation.Validator validator;
    private final Clock clock;

    @Override
    public Mono<RegistrationResult> register(OAuth2AuthenticationToken token, boolean agreedToTerms) {
        var registrationId = token.getAuthorizedClientRegistrationId();
        var oauth2User = token.getPrincipal();
        return systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .filter(SystemSetting.User::isAllowRegistration)
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("The registration is not allowed by the administrator.")))
                .filter(setting -> StringUtils.hasText(setting.getDefaultRole()))
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("The default role is not configured by the administrator.")))
                .flatMap(setting -> checkAgreement(setting, agreedToTerms).thenReturn(setting))
                .flatMap(setting -> connectionService
                        .getByProviderUserId(registrationId, oauth2User.getName())
                        .map(connection -> connection.getSpec().getUsername())
                        .switchIfEmpty(Mono.defer(() -> createUser(setting, registrationId, oauth2User)
                                .map(user -> user.getMetadata().getName()))))
                .flatMap(username -> Mono.zip(
                                userService.getUser(username),
                                systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                        .map(tuple -> new RegistrationResult(
                                username,
                                tuple.getT2().isMustVerifyEmailOnRegistration()
                                        && !tuple.getT1().getSpec().isEmailVerified())));
    }

    private Mono<Void> checkAgreement(SystemSetting.User setting, boolean agreedToTerms) {
        if (CollectionUtils.isEmpty(setting.getRequiredAgreementPages()) || agreedToTerms) {
            return Mono.empty();
        }
        return Mono.error(() -> new AgreementNotAcceptedException(
                "Agreement not accepted.", "problemDetail.user.signup.agreement-not-accepted", null));
    }

    private Mono<User> createUser(SystemSetting.User setting, String registrationId, OAuth2User oauth2User) {
        return resolveUsername(setting, oauth2User)
                .flatMap(username -> resolveEmail(oauth2User)
                        .flatMap(emailCandidate -> {
                            var user = new User();
                            var metadata = new Metadata();
                            metadata.setName(username);
                            user.setMetadata(metadata);
                            var spec = new User.UserSpec();
                            spec.setDisplayName(resolveDisplayName(setting, oauth2User, username));
                            spec.setEmail(emailCandidate.email());
                            spec.setEmailVerified(
                                    emailCandidate.verified() && StringUtils.isNotBlank(emailCandidate.email()));
                            spec.setRegisteredAt(clock.instant());
                            user.setSpec(spec);
                            return userService.createUser(user, Set.of(setting.getDefaultRole()))
                                    .flatMap(created -> connectionService
                                            .createUserConnection(username, registrationId, oauth2User)
                                            .onErrorResume(e -> client.delete(created)
                                                    .then(Mono.<UserConnection>error(e)))
                                            .thenReturn(created));
                        }));
    }

    private Mono<String> resolveUsername(SystemSetting.User setting, OAuth2User oauth2User) {
        var candidate = firstText(
                attribute(oauth2User, "login"),
                attribute(oauth2User, "username"),
                attribute(oauth2User, "user_name"),
                oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null,
                attribute(oauth2User, "nickname"),
                oauth2User.getName());
        return resolveAvailableUsername(setting, candidate)
                .switchIfEmpty(Mono.defer(() -> generateRandomUsername(setting)));
    }

    private Mono<String> resolveAvailableUsername(SystemSetting.User setting, String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Mono.empty();
        }
        var username = candidate.trim().toLowerCase(Locale.ROOT);
        if (!isValidUsername(username) || !isUsernameAllowed(setting, username)) {
            return Mono.empty();
        }
        return client.fetch(User.class, username)
                .hasElement()
                .flatMap(exists -> exists ? Mono.empty() : Mono.just(username));
    }

    private Mono<String> generateRandomUsername(SystemSetting.User setting) {
        return Flux.range(0, RANDOM_USERNAME_MAX_ATTEMPTS)
                .concatMap(i -> {
                    var username = RANDOM_USERNAME_PREFIX
                            + RandomStringUtils.secure().nextAlphanumeric(8).toLowerCase(Locale.ROOT);
                    if (!isValidUsername(username) || !isUsernameAllowed(setting, username)) {
                        return Mono.empty();
                    }
                    return client.fetch(User.class, username)
                            .hasElement()
                            .filter(exists -> !exists)
                            .map(ignored -> username);
                })
                .next()
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("Failed to generate a unique username.")));
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 4
                && username.length() <= 63
                && ValidationUtils.NAME_PATTERN.matcher(username).matches();
    }

    private boolean isUsernameAllowed(SystemSetting.User setting, String username) {
        return !protectedNames(setting).contains(username.toLowerCase(Locale.ROOT));
    }

    private boolean isDisplayNameAllowed(SystemSetting.User setting, String displayName) {
        return !protectedNames(setting).contains(displayName.toLowerCase(Locale.ROOT));
    }

    private Set<String> protectedNames(SystemSetting.User setting) {
        var protectedNames = setting.getProtectedUsernames();
        if (StringUtils.isBlank(protectedNames)) {
            return Set.of();
        }
        return java.util.Arrays.stream(protectedNames.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private String resolveDisplayName(SystemSetting.User setting, OAuth2User oauth2User, String username) {
        var displayName = firstText(
                attribute(oauth2User, "name"),
                attribute(oauth2User, "nickname"),
                attribute(oauth2User, "display_name"),
                oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null,
                username);
        if (!isDisplayNameAllowed(setting, displayName)) {
            return username;
        }
        return displayName;
    }

    private Mono<EmailCandidate> resolveEmail(OAuth2User oauth2User) {
        var email = getEmail(oauth2User);
        if (!isValidEmail(email)) {
            return Mono.just(new EmailCandidate(null, false));
        }
        var normalized = email.toLowerCase(Locale.ROOT);
        return userService.checkEmailAlreadyVerified(normalized)
                .map(occupied -> occupied
                        ? new EmailCandidate(null, false)
                        : new EmailCandidate(normalized, isVerifiedByDefault(oauth2User)));
    }

    private String getEmail(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getClaimAsString("email");
        }
        return attribute(oauth2User, "email");
    }

    private boolean isVerifiedByDefault(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return Boolean.TRUE.equals(oidcUser.getClaimAsBoolean("email_verified"));
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return StringUtils.isNotBlank(email) && validator.validate(new EmailForm(email)).isEmpty();
    }

    private static String attribute(OAuth2User oauth2User, String key) {
        var value = oauth2User.getAttributes().get(key);
        return value instanceof String text ? text : null;
    }

    private static String firstText(String... values) {
        for (var value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private record EmailCandidate(String email, boolean verified) {}

    private record EmailForm(@jakarta.validation.constraints.Email String email) {}
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.oauth2.DefaultOAuth2RegistrationServiceTest"`
Expected: PASS。

- [ ] **Step 5: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/authentication/oauth2/OAuth2RegistrationService.java application/src/main/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationService.java application/src/test/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationServiceTest.java
git commit -m "feat: add OAuth2 registration service"
```

---

## Task 3: 选择页模板与文案

**Files:**
- Create: `application/src/main/resources/templates/login_oauth2_select.html`
- Create: `application/src/main/resources/templates/gateway_fragments/oauth2_select.html`
- Create: `application/src/main/resources/templates/login_oauth2_select.properties`
- Create: `application/src/main/resources/templates/login_oauth2_select_en.properties`
- Create: `application/src/main/resources/templates/login_oauth2_select_es.properties`
- Create: `application/src/main/resources/templates/login_oauth2_select_zh_TW.properties`

- [ ] **Step 1: 创建根模板**

创建 `application/src/main/resources/templates/login_oauth2_select.html`：

```html
<!DOCTYPE html>
<html
  xmlns:th="https://www.thymeleaf.org"
  th:replace="~{gateway_fragments/layout :: layout(title = |#{title} - ${site.title}|, head = null, body = ~{::body})}"
>
  <th:block th:fragment="body">
    <div class="gateway-wrapper">
      <div th:replace="~{gateway_fragments/common::haloLogo}"></div>
      <div class="halo-form-wrapper">
        <h1 class="form-title" th:text="#{title}"></h1>
        <form th:replace="~{gateway_fragments/oauth2_select::form}"></form>
      </div>
      <div th:replace="~{gateway_fragments/common::returnToSiteContent}"></div>
      <div th:replace="~{gateway_fragments/common::languageSwitcher}"></div>
    </div>
  </th:block>
</html>
```

- [ ] **Step 2: 创建表单片段**

创建 `application/src/main/resources/templates/gateway_fragments/oauth2_select.html`：

```html
<form
  th:fragment="form"
  class="halo-form"
  id="oauth2-select-form"
  th:action="@{/login/oauth2/register}"
  method="post"
>
  <style>
    .oauth2-provider-info {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: var(--spacing-2xl);
      padding: 0.75rem 1rem;
      border: 1px solid var(--color-border);
      border-radius: var(--rounded-lg);
      background: #f9fafb;
    }

    .oauth2-provider-info img {
      width: 2em;
      height: 2em;
      border-radius: 9999px;
      flex-shrink: 0;
    }

    .oauth2-provider-info-text {
      display: flex;
      flex-direction: column;
      gap: 0.1em;
    }

    .oauth2-provider-info-name {
      color: var(--color-text);
      font-size: var(--text-base);
      font-weight: 500;
    }

    .oauth2-provider-info-hint {
      color: var(--color-text);
      font-size: var(--text-sm);
      opacity: 0.75;
    }

    .oauth2-bind-button {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 2.5em;
      border: 1px solid var(--color-border);
      border-radius: var(--rounded-base);
      color: var(--color-text);
      font-size: var(--text-base);
      text-decoration: none;
      background: #fff;
    }

    .oauth2-bind-button:hover {
      background: #f3f4f6;
    }
  </style>

  <div class="alert alert-error" role="alert" th:if="${param.error.size() > 0}">
    <strong th:text="#{form.error}"></strong>
  </div>

  <div class="form-item">
    <p th:text="#{form.hint}"></p>
  </div>

  <div class="oauth2-provider-info" th:if="${authProvider != null and authProvider.spec != null}">
    <img th:if="${authProvider.spec.logo != null}" th:src="${authProvider.spec.logo}" alt="" />
    <div class="oauth2-provider-info-text">
      <span class="oauth2-provider-info-name" th:text="${authProvider.spec.displayName}"></span>
      <span class="oauth2-provider-info-hint" th:text="#{form.providerHint}"></span>
    </div>
  </div>

  <div class="form-item-compact" th:if="${agreementPages != null and !agreementPages.isEmpty()}">
    <input type="checkbox" id="agreedToTerms" name="agreedToTerms" value="true" required />
    <label for="agreedToTerms">
      <span th:text="#{form.agreedToTerms.label}"></span>
      <th:block th:each="page, iterStat : ${agreementPages}">
        <a th:if="${page.permalink != null}" th:href="${page.permalink}" target="_blank" th:text="${page.title}"></a>
        <span th:if="${page.permalink == null}" th:text="${page.title}"></span>
        <span th:if="${!iterStat.last}">,</span>
      </th:block>
    </label>
  </div>

  <div class="alert alert-warning" th:if="${!allowRegistration}">
    <strong th:text="#{form.registrationClosed}"></strong>
  </div>

  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />

  <div class="form-item">
    <button type="submit" th:disabled="${!allowRegistration}" th:text="#{form.register}"></button>
  </div>

  <div class="form-item">
    <a class="oauth2-bind-button" th:href="@{/login?oauth2_bind}" th:text="#{form.bind}"></a>
  </div>
</form>
```

- [ ] **Step 3: 创建文案文件**

`login_oauth2_select.properties`：

```properties
title=选择登录方式
form.hint=检测到您尚未绑定账号，请选择以下方式继续：
form.bind=绑定已有账号
form.register=注册新账号
form.agreedToTerms.label=我已阅读并同意
form.providerHint=将使用此账号完成操作
form.registrationClosed=开放注册已关闭，当前仅支持绑定已有账号。
form.error=注册失败，请稍后重试，或选择绑定已有账号。
```

`login_oauth2_select_en.properties`：

```properties
title=Choose How to Continue
form.hint=Your account has not been bound yet. Please choose how to continue:
form.bind=Bind an Existing Account
form.register=Register a New Account
form.agreedToTerms.label=I have read and agree to
form.providerHint=You will continue with this account
form.registrationClosed=Registration is closed. You can only bind an existing account.
form.error=Registration failed. Please try again later or bind an existing account.
```

`login_oauth2_select_es.properties`：

```properties
title=Elige cómo continuar
form.hint=Tu cuenta no está vinculada todavía. Elige cómo continuar:
form.bind=Vincular una cuenta existente
form.register=Registrar una cuenta nueva
form.agreedToTerms.label=He leído y acepto
form.providerHint=Continuarás con esta cuenta
form.registrationClosed=El registro está cerrado. Solo puedes vincular una cuenta existente.
form.error=El registro falló. Inténtalo de nuevo más tarde o vincula una cuenta existente.
```

`login_oauth2_select_zh_TW.properties`：

```properties
title=選擇登入方式
form.hint=偵測到您尚未綁定帳號，請選擇以下方式繼續：
form.bind=綁定既有帳號
form.register=註冊新帳號
form.agreedToTerms.label=我已閱讀並同意
form.providerHint=將使用此帳號完成操作
form.registrationClosed=開放註冊已關閉，目前僅支援綁定既有帳號。
form.error=註冊失敗，請稍後重試，或選擇綁定既有帳號。
```

- [ ] **Step 4: 提交**

```bash
git add application/src/main/resources/templates/login_oauth2_select.html application/src/main/resources/templates/gateway_fragments/oauth2_select.html application/src/main/resources/templates/login_oauth2_select.properties application/src/main/resources/templates/login_oauth2_select_en.properties application/src/main/resources/templates/login_oauth2_select_es.properties application/src/main/resources/templates/login_oauth2_select_zh_TW.properties
git commit -m "feat: add OAuth2 selection page templates"
```

---

## Task 4: 选择页与注册端点

**Files:**
- Modify: `application/src/main/java/run/halo/app/infra/config/WebServerSecurityConfig.java`
- Create: `application/src/main/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpoint.java`
- Test: `application/src/test/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpointTest.java`
- Test: `application/src/test/java/run/halo/app/security/preauth/OAuth2SelectPageIntegrationTest.java`

- [ ] **Step 1: 写失败测试（端点单元测试）**

创建 `application/src/test/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpointTest.java`：

```java
package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.actuator.GlobalInfo;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

@ExtendWith(MockitoExtension.class)
class PreAuthOAuth2RegistrationEndpointTest {

    @Mock
    OAuth2RegistrationService registrationService;

    @Mock
    OAuth2AuthenticationTokenCache tokenCache;

    @Mock
    ServerSecurityContextRepository securityContextRepository;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    LoginHandlerEnhancer loginHandlerEnhancer;

    @Mock
    ServerRequestCache requestCache;

    @Mock
    GlobalInfoService globalInfoService;

    @Mock
    AuthProviderService authProviderService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    ReactiveExtensionClient extensionClient;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        when(globalInfoService.getGlobalInfo()).thenReturn(Mono.just(new GlobalInfo()));
        var endpoint = new PreAuthOAuth2RegistrationEndpoint(
                registrationService,
                tokenCache,
                securityContextRepository,
                userDetailsService,
                loginHandlerEnhancer,
                requestCache,
                globalInfoService,
                authProviderService,
                systemConfigFetcher,
                extensionClient);
        webClient = WebTestClient.bindToRouterFunction(endpoint.preAuthOAuth2RegistrationEndpoints()).build();
    }

    OAuth2AuthenticationToken token() {
        var user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice", "email", "alice@example.com"),
                "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "github");
    }

    @Test
    void shouldRegisterAndRedirectToCompleteProfileWhenEmailIncomplete() {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token()));
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", true)));
        var userDetails = User.withUsername("alice").password("").roles("authenticated").build();
        when(userDetailsService.findByUsername("alice")).thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");
    }

    @Test
    void shouldRedirectToSavedRequestWhenEmailIsComplete() {
        when(tokenCache.getToken(any())).thenReturn(Mono.just(token()));
        when(registrationService.register(any(), eq(false)))
                .thenReturn(Mono.just(new OAuth2RegistrationService.RegistrationResult("alice", false)));
        var userDetails = User.withUsername("alice").password("").roles("authenticated").build();
        when(userDetailsService.findByUsername("alice")).thenReturn(Mono.just(userDetails));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        when(loginHandlerEnhancer.onLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(requestCache.getRedirectUri(any())).thenReturn(Mono.just(URI.create("/uc")));

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");
    }

    @Test
    void shouldRedirectToLoginWhenTokenMissing() {
        when(tokenCache.getToken(any())).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/login/oauth2/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("agreedToTerms=false")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/login");
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.preauth.PreAuthOAuth2RegistrationEndpointTest"`
Expected: FAIL，编译错误（端点类不存在）。

- [ ] **Step 3: 新增 OAuth2AuthenticationTokenCache Bean**

修改 `application/src/main/java/run/halo/app/infra/config/WebServerSecurityConfig.java`，在类内新增：

```java
@Bean
OAuth2AuthenticationTokenCache oauth2AuthenticationTokenCache() {
    return new WebSessionOAuth2AuthenticationTokenCache();
}
```

并在文件头部补充 import：

```java
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.WebSessionOAuth2AuthenticationTokenCache;
```

- [ ] **Step 4: 创建端点类**

创建 `application/src/main/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpoint.java`：

```java
package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

/**
 * Pre-auth endpoints for OAuth2 selection page and registration.
 */
@Component
class PreAuthOAuth2RegistrationEndpoint {

    private final OAuth2RegistrationService registrationService;
    private final OAuth2AuthenticationTokenCache tokenCache;
    private final ServerSecurityContextRepository securityContextRepository;
    private final ReactiveUserDetailsService userDetailsService;
    private final LoginHandlerEnhancer loginHandlerEnhancer;
    private final ServerRequestCache requestCache;
    private final GlobalInfoService globalInfoService;
    private final AuthProviderService authProviderService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final ReactiveExtensionClient extensionClient;

    public PreAuthOAuth2RegistrationEndpoint(
            OAuth2RegistrationService registrationService,
            OAuth2AuthenticationTokenCache tokenCache,
            ServerSecurityContextRepository securityContextRepository,
            ReactiveUserDetailsService userDetailsService,
            LoginHandlerEnhancer loginHandlerEnhancer,
            ServerRequestCache requestCache,
            GlobalInfoService globalInfoService,
            AuthProviderService authProviderService,
            SystemConfigFetcher systemConfigFetcher,
            ReactiveExtensionClient extensionClient) {
        this.registrationService = registrationService;
        this.tokenCache = tokenCache;
        this.securityContextRepository = securityContextRepository;
        this.userDetailsService = userDetailsService;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.requestCache = requestCache;
        this.globalInfoService = globalInfoService;
        this.authProviderService = authProviderService;
        this.systemConfigFetcher = systemConfigFetcher;
        this.extensionClient = extensionClient;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 99)
    RouterFunction<ServerResponse> preAuthOAuth2RegistrationEndpoints() {
        return RouterFunctions.nest(
                path("/login"),
                RouterFunctions.route()
                        .GET("", oauth2SelectParam(), this::renderSelectPage)
                        .POST("/oauth2/register", this::register)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private static RequestPredicate oauth2SelectParam() {
        return request -> request.queryParam("oauth2_select").isPresent();
    }

    private Mono<ServerResponse> renderSelectPage(ServerRequest request) {
        var exchange = request.exchange();
        return tokenCache.getToken(exchange)
                .flatMap(token -> {
                    var registrationId = token.getAuthorizedClientRegistrationId();
                    var providerMono = authProviderService
                            .getEnabledProviders()
                            .filter(ap -> Objects.equals(registrationId, ap.getMetadata().getName()))
                            .next()
                            .defaultIfEmpty(new AuthProvider());
                    var userSettingMono = systemConfigFetcher
                            .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
                    return Mono.zip(providerMono, userSettingMono)
                            .flatMap(tuple -> {
                                var model = new HashMap<String, Object>();
                                model.put("globalInfo", globalInfoService.getGlobalInfo().cache());
                                model.put("authProvider", tuple.getT1());
                                model.put("allowRegistration", tuple.getT2().isAllowRegistration());
                                model.put("agreementPages", fetchAgreementPages().cache());
                                return ServerResponse.ok().render("login_oauth2_select", model);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login"))
                        .build()));
    }

    private Mono<ServerResponse> register(ServerRequest request) {
        var exchange = request.exchange();
        var agreedToTermsMono = request.formData()
                .map(form -> Boolean.parseBoolean(form.getFirst("agreedToTerms")))
                .defaultIfEmpty(false);
        return Mono.zip(tokenCache.getToken(exchange), agreedToTermsMono)
                .flatMap(tuple -> registrationService.register(tuple.getT1(), tuple.getT2())
                        .flatMap(result -> authenticate(exchange, result.username(), tuple.getT1())
                                .thenReturn(result))
                        .flatMap(result -> redirectAfterRegister(exchange, result)))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login"))
                        .build()))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login?oauth2_select&error=registration-failed"))
                        .build());
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, String username, OAuth2AuthenticationToken token) {
        return userDetailsService.findByUsername(username)
                .map(userDetails -> HaloOAuth2AuthenticationToken.authenticated(userDetails, token))
                .flatMap(haloToken -> {
                    var securityContext = new SecurityContextImpl(haloToken);
                    return securityContextRepository.save(exchange, securityContext)
                            .then(loginHandlerEnhancer.onLoginSuccess(exchange, haloToken));
                });
    }

    private Mono<ServerResponse> redirectAfterRegister(
            ServerWebExchange exchange, OAuth2RegistrationService.RegistrationResult result) {
        if (result.needsEmailCompletion()) {
            return ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create("/complete-profile"))
                    .build();
        }
        return requestCache.getRedirectUri(exchange)
                .defaultIfEmpty(URI.create("/uc"))
                .flatMap(uri -> ServerResponse.status(HttpStatus.FOUND).location(uri).build());
    }

    private Mono<List<Map<String, String>>> fetchAgreementPages() {
        return Optional.ofNullable(systemConfigFetcher)
                .map(fetcher -> fetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMapMany(userSetting -> {
                            var pages = userSetting.getRequiredAgreementPages();
                            if (CollectionUtils.isEmpty(pages)) {
                                return Flux.empty();
                            }
                            return Flux.fromIterable(pages);
                        })
                        .flatMap(pageName -> extensionClient.fetch(SinglePage.class, pageName)
                                .map(page -> {
                                    var map = new HashMap<String, String>();
                                    map.put("title", page.getSpec().getTitle());
                                    var status = page.getStatus();
                                    if (status != null) {
                                        map.put("permalink", status.getPermalink());
                                    }
                                    return map;
                                })
                                .onErrorResume(e -> Mono.empty()))
                        .collectList())
                .orElseGet(() -> Mono.just(List.of()));
    }
}
```

- [ ] **Step 5: 运行单元测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.preauth.PreAuthOAuth2RegistrationEndpointTest"`
Expected: PASS。

- [ ] **Step 6: 写选择页渲染集成测试**

创建 `application/src/test/java/run/halo/app/security/preauth/OAuth2SelectPageIntegrationTest.java`：

```java
package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OAuth2SelectPageIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    OAuth2AuthenticationTokenCache tokenCache;

    @Test
    void shouldRenderSelectPageWhenOAuth2TokenCached() {
        var user = new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice", "email", "alice@example.com"),
                "sub");
        when(tokenCache.getToken(any())).thenReturn(
                Mono.just(new OAuth2AuthenticationToken(user, java.util.List.of(), "github")));

        webClient
                .get()
                .uri("/login?oauth2_select")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("选择登录方式"));
    }
}
```

- [ ] **Step 7: 运行集成测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.preauth.OAuth2SelectPageIntegrationTest"`
Expected: PASS。

- [ ] **Step 8: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/infra/config/WebServerSecurityConfig.java application/src/main/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpoint.java application/src/test/java/run/halo/app/security/preauth/PreAuthOAuth2RegistrationEndpointTest.java application/src/test/java/run/halo/app/security/preauth/OAuth2SelectPageIntegrationTest.java application/src/main/java/run/halo/app/security/authentication/oauth2/OAuth2RegistrationService.java application/src/main/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationService.java application/src/test/java/run/halo/app/security/authentication/oauth2/DefaultOAuth2RegistrationServiceTest.java
git commit -m "feat: add OAuth2 selection page and registration endpoint"
```

---

## Task 5: MapOAuth2AuthenticationFilter 重定向变更

**Files:**
- Modify: `application/src/main/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilter.java:106`
- Test: `application/src/test/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilterTest.java`

- [ ] **Step 1: 写失败测试**

创建 `application/src/test/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilterTest.java`：

```java
package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.security.LoginHandlerEnhancer;

class MapOAuth2AuthenticationFilterTest {

    @Test
    void shouldRedirectToSelectPageWhenNotBoundAndNotLoggedIn() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/callback"));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        var user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice"),
                "sub");
        var oauth2Token = new OAuth2AuthenticationToken(user, List.of(), "github");

        var connectionService = mock(UserConnectionService.class);
        when(connectionService.updateUserConnectionIfPresent("github", user)).thenReturn(Mono.empty());
        var securityContextRepository = mock(ServerSecurityContextRepository.class);
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());
        var userDetailsService = mock(ReactiveUserDetailsService.class);
        var loginHandlerEnhancer = mock(LoginHandlerEnhancer.class);
        var filter = new MapOAuth2AuthenticationFilter(
                securityContextRepository, connectionService, userDetailsService, loginHandlerEnhancer);
        var tokenCache = mock(OAuth2AuthenticationTokenCache.class);
        when(tokenCache.saveToken(eq(exchange), any())).thenReturn(Mono.empty());
        filter.setAuthenticationCache(tokenCache);

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(oauth2Token))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/login?oauth2_select"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.oauth2.MapOAuth2AuthenticationFilterTest"`
Expected: FAIL，实际 Location 为 `/login?oauth2_bind`。

- [ ] **Step 3: 修改重定向目标**

修改 `application/src/main/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilter.java` 第 106 行附近：

```java
.then(Mono.defer(() -> redirectStrategy.sendRedirect(
        exchange, URI.create("/login?oauth2_select"))))
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.oauth2.MapOAuth2AuthenticationFilterTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add application/src/main/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilter.java application/src/test/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilterTest.java
git commit -m "feat: redirect unbound OAuth2 users to selection page"
```

---

## Task 6: UserService.checkEmailInUse

**Files:**
- Modify: `api/src/main/java/run/halo/app/core/user/service/UserService.java`
- Modify: `application/src/main/java/run/halo/app/core/user/service/impl/UserServiceImpl.java`
- Test: `application/src/test/java/run/halo/app/core/user/service/impl/UserServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

在 `application/src/test/java/run/halo/app/core/user/service/impl/UserServiceImplTest.java` 中追加：

```java
    @Test
    void shouldCheckEmailInUseExcludingSelf() {
        var otherUser = new User();
        otherUser.setMetadata(new Metadata());
        otherUser.getMetadata().setName("other");
        var otherSpec = new User.UserSpec();
        otherSpec.setEmail("user@example.com");
        otherSpec.setEmailVerified(true);
        otherUser.setSpec(otherSpec);

        var self = new User();
        self.setMetadata(new Metadata());
        self.getMetadata().setName("self");
        var selfSpec = new User.UserSpec();
        selfSpec.setEmail("user@example.com");
        selfSpec.setEmailVerified(true);
        self.setSpec(selfSpec);

        when(client.listAll(eq(User.class), any(), any())).thenReturn(Flux.just(self, otherUser));

        StepVerifier.create(userService.checkEmailInUse("self", "user@example.com"))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(userService.checkEmailInUse("other", "user@example.com"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenEmailNotVerified() {
        var otherUser = new User();
        otherUser.setMetadata(new Metadata());
        otherUser.getMetadata().setName("other");
        var otherSpec = new User.UserSpec();
        otherSpec.setEmail("user@example.com");
        otherSpec.setEmailVerified(false);
        otherUser.setSpec(otherSpec);

        when(client.listAll(eq(User.class), any(), any())).thenReturn(Flux.just(otherUser));

        StepVerifier.create(userService.checkEmailInUse("self", "user@example.com"))
                .expectNext(false)
                .verifyComplete();
    }
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.core.user.service.impl.UserServiceImplTest"`
Expected: FAIL，编译错误（`checkEmailInUse` 不存在）。

- [ ] **Step 3: 实现**

在 `api/src/main/java/run/halo/app/core/user/service/UserService.java` 的 `checkEmailAlreadyVerified` 后追加：

```java
/**
 * Check if the given email is already verified and used by another user.
 *
 * @param username username of the user to exclude
 * @param email email to check
 * @return true if the email is verified and used by another user
 */
Mono<Boolean> checkEmailInUse(String username, String email);
```

在 `application/src/main/java/run/halo/app/core/user/service/impl/UserServiceImpl.java` 的 `checkEmailAlreadyVerified` 后追加：

```java
@Override
public Mono<Boolean> checkEmailInUse(String username, String email) {
    return listByEmail(email)
            .filter(u -> u.getSpec().isEmailVerified())
            .filter(u -> !u.getMetadata().getName().equals(username))
            .hasElements();
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.core.user.service.impl.UserServiceImplTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add api/src/main/java/run/halo/app/core/user/service/UserService.java application/src/main/java/run/halo/app/core/user/service/impl/UserServiceImpl.java application/src/test/java/run/halo/app/core/user/service/impl/UserServiceImplTest.java
git commit -m "feat: add user service method to check email in use excluding self"
```

---

## Task 7: 补邮箱页模板与文案

**Files:**
- Create: `application/src/main/resources/templates/complete_profile.html`
- Create: `application/src/main/resources/templates/gateway_fragments/complete_profile.html`
- Create: `application/src/main/resources/templates/complete_profile.properties`
- Create: `application/src/main/resources/templates/complete_profile_en.properties`
- Create: `application/src/main/resources/templates/complete_profile_es.properties`
- Create: `application/src/main/resources/templates/complete_profile_zh_TW.properties`

- [ ] **Step 1: 创建根模板**

创建 `application/src/main/resources/templates/complete_profile.html`：

```html
<!DOCTYPE html>
<html
  xmlns:th="https://www.thymeleaf.org"
  th:replace="~{gateway_fragments/layout :: layout(title = |#{title} - ${site.title}|, head = null, body = ~{::body})}"
>
  <th:block th:fragment="body">
    <div class="gateway-wrapper">
      <div th:replace="~{gateway_fragments/common::haloLogo}"></div>
      <div class="halo-form-wrapper">
        <h1 class="form-title" th:text="#{title}"></h1>
        <form th:replace="~{gateway_fragments/complete_profile::form}"></form>
      </div>
      <div th:replace="~{gateway_fragments/common::returnToSiteContent}"></div>
      <div th:replace="~{gateway_fragments/common::languageSwitcher}"></div>
    </div>
  </th:block>
</html>
```

- [ ] **Step 2: 创建表单片段**

创建 `application/src/main/resources/templates/gateway_fragments/complete_profile.html`：

```html
<form
  th:fragment="form"
  class="halo-form"
  id="complete-profile-form"
  th:action="@{/complete-profile}"
  th:object="${form}"
  method="post"
>
  <style>
    .form-hint {
      color: var(--color-text);
      font-size: var(--text-sm);
      opacity: 0.8;
      margin: 0 0 var(--spacing-2xl);
    }
  </style>

  <p
    class="form-hint"
    th:text="${mustVerifyEmailOnRegistration} ? #{form.hint.verify} : #{form.hint.save}"
  ></p>

  <div class="form-item">
    <label for="email" th:text="#{form.email.label}"></label>
    <div class="form-input">
      <input
        type="email"
        id="email"
        name="email"
        autocomplete="email"
        spellcheck="false"
        autocorrect="off"
        autocapitalize="off"
        autofocus
        required
        th:field="*{email}"
      />
    </div>
    <p class="alert alert-error" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></p>
  </div>

  <div class="form-item">
    <div class="form-label-group">
      <label for="code" th:text="#{form.code.label}"></label>
      <span class="form-item-extra-link" th:if="${!mustVerifyEmailOnRegistration}" th:text="#{form.code.optional}"></span>
    </div>
    <div class="form-input-group">
      <div class="form-input">
        <input
          type="text"
          inputmode="numeric"
          pattern="\d*"
          autocomplete="one-time-code"
          maxlength="6"
          minlength="6"
          spellcheck="false"
          autocorrect="off"
          autocapitalize="off"
          id="code"
          name="code"
          th:required="${mustVerifyEmailOnRegistration}"
        />
      </div>
      <button id="codeSendButton" type="button" th:text="#{form.code.sendButton}"></button>
    </div>
    <p class="alert alert-error" th:if="${#fields.hasErrors('code')}" th:errors="*{code}"></p>
  </div>

  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  <div class="form-item">
    <button type="submit" th:text="#{form.submit}"></button>
  </div>

  <div class="form-item">
    <a th:href="@{/logout}" class="form-item-extra-link" th:text="#{form.logout}"></a>
  </div>

  <script th:inline="javascript">
    document.addEventListener("DOMContentLoaded", function () {
      const headerName = /*[[${_csrf.headerName}]]*/ "";
      const token = /*[[${_csrf.token}]]*/ "";
      async function sendRequest() {
        const email = document.getElementById("email").value.trim();
        if (!email) {
          throw new Error(/*[[#{form.email.emptyValidation}]]*/ "");
        }
        const response = await fetch("/complete-profile/send-email-code", {
          method: "POST",
          body: JSON.stringify({ email: email }),
          headers: {
            "Content-Type": "application/json",
            [headerName]: token,
          },
        });
        if (!response.ok) {
          const json = await response.json();
          throw new Error(json.detail || /*[[#{form.error.sendCodeFailed}]]*/ "");
        }
        return response;
      }
      const emailCodeSendButton = document.getElementById("codeSendButton");
      sendVerificationCode(emailCodeSendButton, sendRequest);
    });
  </script>
</form>
```

- [ ] **Step 3: 创建文案文件**

`complete_profile.properties`：

```properties
title=完善资料
form.hint.verify=为保证账号安全，请验证您的邮箱后继续。
form.hint.save=请填写您的邮箱地址，以便接收系统通知。
form.email.label=邮箱
form.code.label=验证码
form.code.optional=选填
form.code.sendButton=发送验证码
form.submit=保存并继续
form.email.emptyValidation=请输入邮箱
form.error.sendCodeFailed=发送验证码失败，请稍后重试。
form.logout=退出登录
```

字段错误文案（`complete-profile.error.email-in-use`、`complete-profile.error.code-required`、`complete-profile.error.invalid-code`）需要添加到 `config/i18n/messages*.properties`，因为 `th:errors` 通过 Spring MessageSource 解析错误码，模板内 `form.error.*` 键不会被 `#fields.errors` 使用。

`complete_profile_en.properties`：

```properties
title=Complete Your Profile
form.hint.verify=For account security, please verify your email address to continue.
form.hint.save=Add your email address to receive notifications from the site.
form.email.label=Email
form.code.label=Verification Code
form.code.optional=Optional
form.code.sendButton=Send Code
form.submit=Save and Continue
form.email.emptyValidation=Please enter your email
form.error.sendCodeFailed=Failed to send verification code. Please try again later.
form.logout=Logout
```

`complete_profile_es.properties`：

```properties
title=Completa tu perfil
form.hint.verify=Por seguridad de tu cuenta, verifica tu correo electrónico para continuar.
form.hint.save=Añade tu correo electrónico para recibir notificaciones del sitio.
form.email.label=Correo electrónico
form.code.label=Código de verificación
form.code.optional=Opcional
form.code.sendButton=Enviar código
form.submit=Guardar y continuar
form.email.emptyValidation=Introduce tu correo electrónico
form.error.sendCodeFailed=Error al enviar el código de verificación. Inténtalo de nuevo más tarde.
form.logout=Cerrar sesión
```

`complete_profile_zh_TW.properties`：

```properties
title=完善資料
form.hint.verify=為確保帳號安全，請驗證您的電子郵件後繼續。
form.hint.save=請填寫您的電子郵件地址，以便接收網站通知。
form.email.label=電子郵件
form.code.label=驗證碼
form.code.optional=選填
form.code.sendButton=發送驗證碼
form.submit=儲存並繼續
form.email.emptyValidation=請輸入電子郵件
form.error.sendCodeFailed=發送驗證碼失敗，請稍後重試。
form.logout=登出
```

- [ ] **Step 4: 提交**

```bash
git add application/src/main/resources/templates/complete_profile.html application/src/main/resources/templates/gateway_fragments/complete_profile.html application/src/main/resources/templates/complete_profile.properties application/src/main/resources/templates/complete_profile_en.properties application/src/main/resources/templates/complete_profile_es.properties application/src/main/resources/templates/complete_profile_zh_TW.properties
git commit -m "feat: add complete profile page templates"
```

---

## Task 8: 补邮箱端点

**Files:**
- Create: `application/src/main/java/run/halo/app/security/completion/EmailCompletionEndpoint.java`
- Test: `application/src/test/java/run/halo/app/security/completion/EmailCompletionEndpointIntegrationTest.java`

- [ ] **Step 1: 写失败测试**

创建 `application/src/test/java/run/halo/app/security/completion/EmailCompletionEndpointIntegrationTest.java`：

```java
package run.halo.app.security.completion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "fake-user", roles = "authenticated")
class EmailCompletionEndpointIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoBean
    SystemConfigFetcher systemConfigFetcher;

    @MockitoBean
    EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        webClient = webClient.mutateWith(SecurityMockServerConfigurers.csrf());
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        createUser("fake-user", null, false);
    }

    void createUser(String name, String email, boolean verified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        var spec = new User.UserSpec();
        spec.setDisplayName("Fake User");
        spec.setEmail(email);
        spec.setEmailVerified(verified);
        spec.setRegisteredAt(Instant.now());
        user.setSpec(spec);
        client.create(user).block();
    }

    @Test
    void shouldRenderPage() {
        webClient
                .get()
                .uri("/complete-profile")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("完善资料"));
    }

    @Test
    void shouldRejectWithoutCodeWhenVerificationRequired() {
        webClient
                .post()
                .uri("/complete-profile")
                .bodyValue("email=fake%40example.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("验证码"));
    }

    @Test
    void shouldVerifyEmailAndRedirect() {
        when(emailVerificationService.verify(eq("fake-user"), eq("123456"))).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/complete-profile")
                .bodyValue("email=fake%40example.com&code=123456")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        verify(emailVerificationService).verify(eq("fake-user"), eq("123456"));
    }

    @Test
    void shouldSendEmailCode() {
        when(emailVerificationService.sendVerificationCode(anyString(), anyString())).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/complete-profile/send-email-code")
                .bodyValue("{\"email\":\"fake@example.com\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .isAccepted();

        verify(emailVerificationService).sendVerificationCode(eq("fake-user"), eq("fake@example.com"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.completion.EmailCompletionEndpointIntegrationTest"`
Expected: FAIL，`/complete-profile` 无路由。

- [ ] **Step 3: 实现端点**

创建 `application/src/main/java/run/halo/app/security/completion/EmailCompletionEndpoint.java`：

```java
package run.halo.app.security.completion;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;

/**
 * Gateway endpoints for completing the authenticated user's email.
 */
@Component
@RequiredArgsConstructor
class EmailCompletionEndpoint {

    private final UserService userService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final EmailVerificationService emailVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ReactiveExtensionClient client;
    private final ServerRequestCache requestCache;
    private final GlobalInfoService globalInfoService;
    private final Validator validator;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> emailCompletionEndpoints() {
        return RouterFunctions.nest(
                path("/complete-profile"),
                RouterFunctions.route()
                        .GET("", this::page)
                        .POST("", this::submit)
                        .POST("/send-email-code", this::sendEmailCode)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private Mono<ServerResponse> page(ServerRequest request) {
        return currentUsername(request.exchange())
                .flatMap(username -> userService.getUser(username))
                .flatMap(user -> systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMap(setting -> {
                            var form = new CompleteProfileForm(user.getSpec().getEmail(), null);
                            var bindingResult = new BeanPropertyBindingResult(form, "form");
                            var model = bindingResult.getModel();
                            model.put("globalInfo", globalInfoService.getGlobalInfo().cache());
                            model.put("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
                            return ServerResponse.ok().render("complete_profile", model);
                        }));
    }

    private Mono<ServerResponse> submit(ServerRequest request) {
        var exchange = request.exchange();
        return request.formData()
                .flatMap(form -> {
                    var completeForm = new CompleteProfileForm(form.getFirst("email"), form.getFirst("code"));
                    var bindingResult = validate(completeForm, "form", validator, exchange);
                    return currentUsername(exchange)
                            .flatMap(username -> handleSubmit(exchange, username, completeForm, bindingResult));
                });
    }

    private Mono<ServerResponse> handleSubmit(
            ServerWebExchange exchange,
            String username,
            CompleteProfileForm form,
            BindingResult bindingResult) {
        return Mono.zip(
                        userService.getUser(username),
                        systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .flatMap(tuple -> {
                    var user = tuple.getT1();
                    var setting = tuple.getT2();
                    if (bindingResult.hasErrors()) {
                        return renderPage(exchange, setting, form, bindingResult);
                    }
                    var email = form.email().toLowerCase(Locale.ROOT);
                    return userService.checkEmailInUse(username, email)
                            .flatMap(inUse -> {
                                if (inUse) {
                                    bindingResult.rejectValue(
                                            "email", "complete-profile.error.email-in-use", "Email is already in use");
                                    return renderPage(exchange, setting, form, bindingResult);
                                }
                                if (setting.isMustVerifyEmailOnRegistration() && StringUtils.isBlank(form.code())) {
                                    bindingResult.rejectValue(
                                            "code", "complete-profile.error.code-required", "Email code is required");
                                    return renderPage(exchange, setting, form, bindingResult);
                                }
                                return verifyOrSave(
                                        exchange, username, user, email, form.code(), setting, form, bindingResult);
                            });
                });
    }

    private Mono<ServerResponse> verifyOrSave(
            ServerWebExchange exchange,
            String username,
            User user,
            String email,
            String code,
            SystemSetting.User setting,
            CompleteProfileForm form,
            BindingResult bindingResult) {
        if (StringUtils.isNotBlank(code)) {
            return emailVerificationService.verify(username, code)
                    .then(redirectToTarget(exchange))
                    .onErrorResume(EmailVerificationFailed.class, e -> {
                        bindingResult.rejectValue(
                                "code", "complete-profile.error.invalid-code", "Invalid email code");
                        return renderPage(exchange, setting, form, bindingResult);
                    });
        }
        user.getSpec().setEmail(email);
        return client.update(user).then(redirectToTarget(exchange));
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        var exchange = request.exchange();
        return request.bodyToMono(SendEmailCodeBody.class)
                .flatMap(body -> {
                    var bindingResult = validate(body, "body", validator, exchange);
                    if (bindingResult.hasErrors()) {
                        return Mono.error(new RequestBodyValidationException(bindingResult));
                    }
                    var email = body.email().toLowerCase(Locale.ROOT);
                    return currentUsername(exchange)
                            .flatMap(username -> userService.checkEmailInUse(username, email)
                                    .flatMap(inUse -> {
                                        if (inUse) {
                                            return Mono.error(new EmailVerificationFailed(
                                                    "Email already in use.",
                                                    null,
                                                    "problemDetail.user.email.verify.emailInUse",
                                                    null));
                                        }
                                        return emailVerificationService
                                                .sendVerificationCode(username, email)
                                                .transformDeferred(sendCodeRateLimiter(exchange))
                                                .onErrorMap(
                                                        RequestNotPermitted.class,
                                                        RateLimitExceededException::new);
                                    }));
                })
                .then(ServerResponse.accepted().build());
    }

    private Mono<ServerResponse> renderPage(
            ServerWebExchange exchange,
            SystemSetting.User setting,
            CompleteProfileForm form,
            BindingResult bindingResult) {
        var model = new HashMap<String, Object>();
        model.put("globalInfo", globalInfoService.getGlobalInfo().cache());
        model.put("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
        model.putAll(bindingResult.getModel());
        return ServerResponse.ok().render("complete_profile", model);
    }

    private Mono<ServerResponse> redirectToTarget(ServerWebExchange exchange) {
        return requestCache.getRedirectUri(exchange)
                .defaultIfEmpty(URI.create("/uc"))
                .flatMap(uri -> ServerResponse.status(HttpStatus.FOUND).location(uri).build());
    }

    private Mono<String> currentUsername(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(java.security.Principal::getName);
    }

    private <T> RateLimiterOperator<T> sendCodeRateLimiter(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiterKey = "send-email-verification-code-from-" + clientIp;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    public record CompleteProfileForm(
            @NotBlank @Email String email, String code) {}

    public record SendEmailCodeBody(@NotBlank @Email String email) {}
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.completion.EmailCompletionEndpointIntegrationTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/completion/EmailCompletionEndpoint.java application/src/test/java/run/halo/app/security/completion/EmailCompletionEndpointIntegrationTest.java
git commit -m "feat: add complete profile email endpoints"
```

---

## Task 9: 门禁过滤器

**Files:**
- Create: `application/src/main/java/run/halo/app/security/completion/EmailCompletionFilter.java`
- Create: `application/src/main/java/run/halo/app/security/completion/EmailCompletionSecurityConfigurer.java`
- Modify: `application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java`
- Test: `application/src/test/java/run/halo/app/security/completion/EmailCompletionFilterTest.java`
- Test: `application/src/test/java/run/halo/app/security/completion/EmailCompletionFilterIntegrationTest.java`

- [ ] **Step 1: 写失败测试（单元）**

创建 `application/src/test/java/run/halo/app/security/completion/EmailCompletionFilterTest.java`：

```java
package run.halo.app.security.completion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

class EmailCompletionFilterTest {

    UserService userService;
    SystemConfigFetcher systemConfigFetcher;
    ServerRequestCache requestCache;
    ServerResponse.Context responseContext;
    EmailCompletionFilter filter;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        systemConfigFetcher = mock(SystemConfigFetcher.class);
        requestCache = mock(ServerRequestCache.class);
        responseContext = mock(ServerResponse.Context.class);
        when(responseContext.messageWriters()).thenReturn(List.of(new Jackson2JsonEncoder()));
        when(responseContext.viewResolvers()).thenReturn(List.of());
        filter = new EmailCompletionFilter(systemConfigFetcher, userService, requestCache, responseContext);
    }

    SystemSetting.User setting(boolean mustVerify) {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(mustVerify);
        return setting;
    }

    User user(boolean emailVerified) {
        var user = new User();
        user.setMetadata(new run.halo.app.extension.Metadata());
        user.getMetadata().setName("user");
        var spec = new User.UserSpec();
        spec.setEmail("user@example.com");
        spec.setEmailVerified(emailVerified);
        user.setSpec(spec);
        return user;
    }

    @Test
    void shouldRedirectHtmlRequestToCompleteProfile() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(false)));
        when(requestCache.saveRequest(any())).thenReturn(Mono.empty());

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create("/complete-profile"));
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldReturnForbiddenWithTypeForJsonRequest() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(false)));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/apis/test").accept(MediaType.APPLICATION_JSON));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"type\":\"email-not-set\"");
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldSkipWhenVerificationNotRequired() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(false)));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldSkipForSuperAdmin() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("admin", "password", "ROLE_super-role")))
                .block();

        verify(chain).filter(exchange);
        verify(systemConfigFetcher, never()).fetch(any(), any());
    }

    @Test
    void shouldSkipForExemptPath() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/logout").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldSkipWhenEmailVerified() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting(true)));
        when(userService.getUser("user")).thenReturn(Mono.just(user(true)));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console").accept(MediaType.TEXT_HTML));
        var chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken("user", "password", "ROLE_authenticated")))
                .block();

        verify(chain).filter(exchange);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.completion.EmailCompletionFilterTest"`
Expected: FAIL，编译错误（过滤器不存在）。

- [ ] **Step 3: 实现过滤器与配置器**

创建 `application/src/main/java/run/halo/app/security/completion/EmailCompletionFilter.java`：

```java
package run.halo.app.security.completion;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * Redirects or rejects authenticated users who must complete a verified email.
 */
public class EmailCompletionFilter implements WebFilter {

    private static final URI EMAIL_NOT_SET_TYPE = URI.create("email-not-set");

    private static final List<String> EXEMPT_PATH_PREFIXES = List.of(
            "/oauth2",
            "/login",
            "/signup",
            "/password-reset",
            "/logout",
            "/complete-profile",
            "/system/setup",
            "/error",
            "/assets",
            "/images",
            "/js",
            "/styles",
            "/webjars",
            "/favicon.");

    private final SystemConfigFetcher systemConfigFetcher;
    private final UserService userService;
    private final ServerRequestCache requestCache;
    private final ServerResponse.Context responseContext;
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    public EmailCompletionFilter(
            SystemConfigFetcher systemConfigFetcher,
            UserService userService,
            ServerRequestCache requestCache,
            ServerResponse.Context responseContext) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.userService = userService;
        this.requestCache = requestCache;
        this.responseContext = responseContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(this::isFullyAuthenticated)
                .flatMap(authentication -> shouldIntercept(exchange, authentication))
                .flatMap(intercept -> intercept ? intercept(exchange) : chain.filter(exchange))
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    private boolean isFullyAuthenticated(Authentication authentication) {
        return authentication.isAuthenticated() && !trustResolver.isAnonymous(authentication);
    }

    private Mono<Boolean> shouldIntercept(ServerWebExchange exchange, Authentication authentication) {
        if (isSuperAdmin(authentication) || isExemptPath(exchange)) {
            return Mono.just(false);
        }
        return systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .map(SystemSetting.User::isMustVerifyEmailOnRegistration)
                .defaultIfEmpty(false)
                .flatMap(required -> {
                    if (!required) {
                        return Mono.just(false);
                    }
                    return userService.getUser(authentication.getName())
                            .map(user -> !user.getSpec().isEmailVerified())
                            .onErrorReturn(false);
                });
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return AuthorityUtils.authoritiesToRoles(authentication.getAuthorities())
                .contains(AuthorityUtils.SUPER_ROLE_NAME);
    }

    private boolean isExemptPath(ServerWebExchange exchange) {
        var path = exchange.getRequest().getPath().pathWithinApplication().value();
        return EXEMPT_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> intercept(ServerWebExchange exchange) {
        if (isHtmlRequest(exchange)) {
            return requestCache.saveRequest(exchange)
                    .then(redirectStrategy.sendRedirect(exchange, URI.create("/complete-profile")));
        }
        return writeForbidden(exchange);
    }

    private boolean isHtmlRequest(ServerWebExchange exchange) {
        if (!HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        if (HaloUtils.isXhr(exchange.getRequest().getHeaders())) {
            return false;
        }
        return exchange.getRequest().getHeaders().getAccept().stream()
                .anyMatch(mediaType -> mediaType.includes(MediaType.TEXT_HTML));
    }

    private Mono<Void> writeForbidden(ServerWebExchange exchange) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Email is not set or verified.");
        problemDetail.setType(EMAIL_NOT_SET_TYPE);
        problemDetail.setTitle("Email Not Set");
        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyValue(problemDetail)
                .flatMap(response -> response.writeTo(exchange, responseContext));
    }
}
```

创建 `application/src/main/java/run/halo/app/security/completion/EmailCompletionSecurityConfigurer.java`：

```java
package run.halo.app.security.completion;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * Registers {@link EmailCompletionFilter} after authentication.
 */
@Component
@Order(10)
class EmailCompletionSecurityConfigurer implements SecurityConfigurer {

    private final SystemConfigFetcher systemConfigFetcher;
    private final UserService userService;
    private final ServerRequestCache serverRequestCache;
    private final ServerResponse.Context context;

    public EmailCompletionSecurityConfigurer(
            SystemConfigFetcher systemConfigFetcher,
            UserService userService,
            ServerRequestCache serverRequestCache,
            ServerResponse.Context context) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.userService = userService;
        this.serverRequestCache = serverRequestCache;
        this.context = context;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new EmailCompletionFilter(
                systemConfigFetcher, userService, serverRequestCache, context);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }
}
```

修改 `application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java` 的 `preAuthenticationAuthorizationConfigurer`，在 `/login/impersonate` 规则后追加：

```java
.pathMatchers("/complete-profile/**")
.authenticated()
```

- [ ] **Step 4: 运行单元测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.completion.EmailCompletionFilterTest"`
Expected: PASS。

- [ ] **Step 5: 写集成测试**

创建 `application/src/test/java/run/halo/app/security/completion/EmailCompletionFilterIntegrationTest.java`：

```java
package run.halo.app.security.completion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "fake-user", roles = "authenticated")
class EmailCompletionFilterIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoBean
    SystemConfigFetcher systemConfigFetcher;

    @BeforeEach
    void setUp() {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        createUser("fake-user", null, false);
    }

    void createUser(String name, String email, boolean verified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        var spec = new User.UserSpec();
        spec.setDisplayName("Fake User");
        spec.setEmail(email);
        spec.setEmailVerified(verified);
        spec.setRegisteredAt(Instant.now());
        user.setSpec(spec);
        client.create(user).block();
    }

    @Test
    void shouldRedirectHtmlRequestToCompleteProfile() {
        webClient
                .get()
                .uri("/uc")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");
    }

    @Test
    void shouldReturnForbiddenForJsonRequest() {
        webClient
                .get()
                .uri("/apis/api.console.halo.run/v1alpha1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("email-not-set");
    }
}
```

- [ ] **Step 6: 运行集成测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.completion.EmailCompletionFilterIntegrationTest"`
Expected: PASS。

- [ ] **Step 7: 提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/completion application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java application/src/test/java/run/halo/app/security/completion
git commit -m "feat: add email completion gate filter"
```

---

## Task 10: 重新生成 api-client 并全量验证

**Files:**
- Modify: `ui/packages/api-client/src/models/user-spec.ts`（由生成器更新）

- [ ] **Step 1: 重新生成 OpenAPI 与 api-client**

Run:

```bash
./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen
```

Expected: `ui/packages/api-client/src/models/user-spec.ts` 中 `email` 变为可选（`email?: string`）。

- [ ] **Step 2: 前端类型检查与 lint**

Run:

```bash
pnpm -C ui typecheck && pnpm -C ui lint
```

Expected: PASS。

- [ ] **Step 3: 后端全量测试**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 4: 提交**

```bash
git add ui/packages/api-client/src
git commit -m "chore: regenerate api client for optional user email"
```

---

## 自审结论

- Spec 覆盖：选择页（Task 3/4）、注册规则（Task 2）、门禁（Task 9）、补邮箱页（Task 7/8）、`email` 可选（Task 1）、重定向变更（Task 5）、`checkEmailInUse`（Task 6）、api-client（Task 10）全部有对应任务。
- 无占位符：所有步骤均含可执行代码与命令。
- 类型一致：`RegistrationResult(username, needsEmailCompletion)` 在服务与端点中一致；`checkEmailInUse` 签名一致；过滤器构造函数与测试一致。

