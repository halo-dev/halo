# 安全验证（Security Verification）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修改密码前，通过 GitHub 式「安全验证」页（邮箱验证码或 TOTP，主题模板实现）验证用户身份；验证通过后会话获得 30 分钟有效期的已验证标记。

**Architecture:** 新增服务端渲染的 Thymeleaf 验证页 `/security-verification`（与 2FA challenge 页同机制，主题可覆盖），验证通过后向 WebSession 写入通用 sudo 标记；`UcUserEndpoint.changeMyPassword` 在需要验证时检查该标记（403 + redirectURI）。TOTP 校验从 `TwoFactorAuthEndpoint` 抽取为共享服务；邮箱验证码复用 `EmailVerificationService` 的内存码机制（10 分钟过期、5 次尝试、黑名单），新增 `security-verification` 通知原因类型与模板。

**Tech Stack:** Spring WebFlux（RouterFunction + Thymeleaf）、Spring Security（WebSession、CSRF）、ReactiveExtensionClient、resilience4j 限流、Vue 3（UC Profile 接线）。

## Global Constraints

- 仅**已有密码**的用户需要安全验证；passwordless 用户首次设置密码不要求验证。
- 有密码但**既无已验证邮箱也无 TOTP** 的用户回退到仅原密码验证。
- 两种方式都可用时用户任选其一；任一通过即可。
- 会话标记 TTL 固定 30 分钟（常量，不配置化）。
- 验证页为服务端渲染 Thymeleaf 模板（`application/src/main/resources/templates/`），**不新增 Vue 页面**；主题可覆盖。
- `redirect` 参数仅接受站内相对路径（`/` 开头、非 `//`、不含 `\`——浏览器会把 `\` 规范化为 `/` 形成协议相对外链），非法值回退 `/uc/profile`。
- 全链路 reactive，禁止阻塞 I/O。
- 契约变更后必须 `./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen` 重新生成，**绝不手改 `ui/packages/api-client/src/`**。
- 每次提交前运行 `./gradlew spotlessApply`；只 stage 本次任务涉及的文件。
- 提交信息遵循现有风格（`feat:` / `refactor:` / `docs:`）。

---

### Task 1: 抽取 `TotpVerificationService`

**Files:**
- Create: `application/src/main/java/run/halo/app/security/authentication/twofactor/TotpVerificationService.java`
- Modify: `application/src/main/java/run/halo/app/security/authentication/twofactor/TwoFactorAuthEndpoint.java`（改用共享服务，删除 `validateTotpCode(User, String)` 私有方法）
- Test: `application/src/test/java/run/halo/app/security/authentication/twofactor/TotpVerificationServiceTest.java`

**Interfaces:**
- Consumes: `TotpAuthService`（`decryptSecret(String)`、`validateTotp(String, int)`）、`User.getSpec().getTotpEncryptedSecret()`
- Produces: `TotpVerificationService.validate(User user, String totpCode) → Mono<Void>`（Task 6 使用）

- [ ] **Step 1: 写失败测试** `TotpVerificationServiceTest.java`

```java
package run.halo.app.security.authentication.twofactor;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@ExtendWith(MockitoExtension.class)
class TotpVerificationServiceTest {

    @Mock
    TotpAuthService totpAuthService;

    @InjectMocks
    TotpVerificationService service;

    User user(String encryptedSecret) {
        var spec = new UserSpec();
        spec.setTotpEncryptedSecret(encryptedSecret);
        var user = new User();
        user.setSpec(spec);
        user.setMetadata(new Metadata());
        return user;
    }

    @Test
    void shouldPassWhenTotpNotConfigured() {
        StepVerifier.create(service.validate(user(null), null)).verifyComplete();
    }

    @Test
    void shouldFailWhenCodeMissing() {
        StepVerifier.create(service.validate(user("encrypted"), null))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeNotNumeric() {
        StepVerifier.create(service.validate(user("encrypted"), "abc"))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeInvalid() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(false);
        StepVerifier.create(service.validate(user("encrypted"), "123456"))
                .expectError(ServerWebInputException.class)
                .verify();
    }

    @Test
    void shouldPassWhenCodeValid() {
        when(totpAuthService.decryptSecret("encrypted")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(true);
        StepVerifier.create(service.validate(user("encrypted"), "123456")).verifyComplete();
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.twofactor.TotpVerificationServiceTest"`
Expected: 编译失败，`TotpVerificationService` 不存在。

- [ ] **Step 3: 实现** `TotpVerificationService.java`

```java
package run.halo.app.security.authentication.twofactor;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

/**
 * TOTP verification for a user. Shared by 2FA settings endpoints and security verification.
 */
@Component
@RequiredArgsConstructor
public class TotpVerificationService {

    private final TotpAuthService totpAuthService;

    /**
     * Validate the TOTP code of the given user. Passes when TOTP is not configured.
     */
    public Mono<Void> validate(User user, String totpCode) {
        var totpEncryptedSecret = user.getSpec().getTotpEncryptedSecret();
        if (StringUtils.isBlank(totpEncryptedSecret)) {
            // TOTP is not configured, no need to validate
            return Mono.empty();
        }
        if (!StringUtils.hasText(totpCode)) {
            return Mono.error(new ServerWebInputException("TOTP code is required"));
        }
        int code;
        try {
            code = Integer.parseInt(totpCode);
        } catch (NumberFormatException e) {
            return Mono.error(new ServerWebInputException("Invalid TOTP code"));
        }
        var rawSecret = totpAuthService.decryptSecret(totpEncryptedSecret);
        if (!totpAuthService.validateTotp(rawSecret, code)) {
            return Mono.error(new ServerWebInputException("Invalid TOTP code"));
        }
        return Mono.empty();
    }
}
```

- [ ] **Step 4: 改造 `TwoFactorAuthEndpoint` 使用共享服务**

- 构造函数注入 `TotpVerificationService`（`totpVerificationService` 字段），保留 `TotpAuthService`（`configureTotp`/`getTotpAuthLink` 仍需要）。

- 删除私有方法 `validateTotpCode(User user, String totpCode)`（第 290-297 行）；三处调用 `validateTotpCode(user, ...)` 改为 `totpVerificationService.validate(user, ...)`：

  - `deleteTotp` 中 `delayUntil(user -> validateTotpCode(user, passwordRequest.getTotpCode()))`
  - `toggleTwoFactor` 中 `delayUntil(user -> validateTotpCode(user, passwordRequest.getTotpCode()))`
  - `configureTotp` 中 `delayUntil(user -> validateTotpCode(user, totpRequest.getCurrentTotpCode()))`
- **保留** `validateTotpCode(String totpEncryptedSecret, String totpCode)`（第 299-314 行）——`configureTotp` 用它校验新 secret 的 code，语义不同。
- [ ] **Step 5: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.authentication.twofactor.TotpVerificationServiceTest" && ./gradlew :application:compileJava`
Expected: PASS，编译通过。

- [ ] **Step 6: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/authentication/twofactor/TotpVerificationService.java \
  application/src/main/java/run/halo/app/security/authentication/twofactor/TwoFactorAuthEndpoint.java \
  application/src/test/java/run/halo/app/security/authentication/twofactor/TotpVerificationServiceTest.java
git commit -m "refactor: extract TOTP validation into TotpVerificationService"
```

---

### Task 2: `EmailVerificationService` 安全验证码方法 + 通知原因/模板

**Files:**
- Modify: `application/src/main/java/run/halo/app/core/user/service/EmailVerificationService.java`（接口加 2 个方法）
- Modify: `application/src/main/java/run/halo/app/core/user/service/impl/EmailVerificationServiceImpl.java`
- Modify: `application/src/main/resources/extensions/notification.yaml`（ReasonType `security-verification`）
- Modify: `application/src/main/resources/extensions/notification-templates.yaml`（NotificationTemplate `template-security-verification`）
- Test: `application/src/test/java/run/halo/app/core/user/service/impl/EmailVerificationServiceImplTest.java`（新增 `@Nested class SecurityVerificationCodeTest`）

**Interfaces:**
- Consumes: `EmailVerificationManager`（类内私有）、`NotificationReasonEmitter`、`NotificationCenter`、`ReactiveExtensionClient`
- Produces: `EmailVerificationService.sendSecurityVerificationCode(String username) → Mono<Void>`、`EmailVerificationService.verifySecurityVerificationCode(String username, String code) → Mono<Void>`（Task 6 使用）

- [ ] **Step 1: 接口加方法** `EmailVerificationService.java`（在 `verify` 方法后追加）

```java
    /**
     * Send a security verification code to the verified email of the given user.
     *
     * @param username username of the user must not be blank
     */
    Mono<Void> sendSecurityVerificationCode(String username);

    /**
     * Verify the security verification code of the given user. The code is removed after verification
     * and the email binding is never changed.
     *
     * @param username username of the user must not be blank
     * @param code code to verify must not be blank
     * @throws run.halo.app.infra.exception.EmailVerificationFailed if the code is invalid or too many attempts
     */
    Mono<Void> verifySecurityVerificationCode(String username, String code);
```

- [ ] **Step 2: 写失败测试**（追加到 `EmailVerificationServiceImplTest.java`）

```java
    @Nested
    class SecurityVerificationCodeTest {

        @Mock
        ReactiveExtensionClient client;

        @Mock
        NotificationReasonEmitter reasonEmitter;

        @Mock
        NotificationCenter notificationCenter;

        EmailVerificationServiceImpl service;

        @BeforeEach
        void setUp() {
            service = new EmailVerificationServiceImpl(client, reasonEmitter, notificationCenter);
        }

        User user(boolean emailVerified, String email) {
            var spec = new User.UserSpec();
            spec.setEmail(email);
            spec.setEmailVerified(emailVerified);
            var user = new User();
            user.setSpec(spec);
            var metadata = new Metadata();
            metadata.setName("faker");
            user.setMetadata(metadata);
            return user;
        }

        EmailVerificationManager manager() {
            return ReflectionTestUtils.getField(service, "emailVerificationManager", EmailVerificationManager.class);
        }

        @Test
        void shouldRejectWhenEmailNotVerified() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(false, "faker@halo.run")));
            StepVerifier.create(service.sendSecurityVerificationCode("faker"))
                    .expectError(ServerWebInputException.class)
                    .verify();
        }

        @Test
        void shouldSendCodeWhenEmailVerified() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            when(reasonEmitter.emit(eq(EmailVerificationServiceImpl.SECURITY_VERIFICATION_REASON_TYPE), any()))
                    .thenReturn(Mono.empty());
            when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.just(new Subscription()));
            StepVerifier.create(service.sendSecurityVerificationCode("faker")).verifyComplete();
            assertThat(manager().contains("faker", "faker@halo.run")).isTrue();
        }

        @Test
        void shouldVerifyCodeAndRemoveIt() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            var code = manager().generateCode("faker", "faker@halo.run");
            StepVerifier.create(service.verifySecurityVerificationCode("faker", code)).verifyComplete();
            assertThat(manager().contains("faker", "faker@halo.run")).isFalse();
        }

        @Test
        void shouldRejectInvalidCode() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            manager().generateCode("faker", "faker@halo.run");
            StepVerifier.create(service.verifySecurityVerificationCode("faker", "000000"))
                    .expectError(EmailVerificationFailed.class)
                    .verify();
        }
    }
```

需要新增 import（追加到文件头部）：

```java
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
```

注意：`EmailVerificationManager` 与 `manager()` 需要把 impl 的私有字段改为包级可见（见 Step 3 说明）；`EmailVerificationManager.contains` 已是包级方法。另：`sendSecurityVerificationCode` 的实现类名为 `EmailVerificationServiceImpl`（不新建子类），`SECURITY_VERIFICATION_REASON_TYPE` 定义为该类的 `public static final` 常量。

- [ ] **Step 3: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.core.user.service.impl.EmailVerificationServiceImplTest"`
Expected: 编译失败（方法/常量不存在）。

- [ ] **Step 4: 实现** `EmailVerificationServiceImpl.java`

- 类中新增常量（与 `EMAIL_VERIFICATION_REASON_TYPE` 并列）：

```java
public static final String SECURITY_VERIFICATION_REASON_TYPE = "security-verification";
```

- 将私有字段改为包级可见（供测试通过 `ReflectionTestUtils` 读取，测试与 impl 同包）：

```java
final EmailVerificationManager emailVerificationManager = new EmailVerificationManager();
```

- 新增方法（放在 `verify` 之后）：

```java
    @Override
    public Mono<Void> sendSecurityVerificationCode(String username) {
        Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
        return Mono.defer(() -> client.get(User.class, username).flatMap(user -> {
            var email = user.getSpec().getEmail();
            if (!user.getSpec().isEmailVerified() || StringUtils.isBlank(email)) {
                return Mono.error(new ServerWebInputException("Email is not verified."));
            }
            return sendSecurityVerificationNotification(username, email);
        }));
    }

    @Override
    public Mono<Void> verifySecurityVerificationCode(String username, String code) {
        Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.defer(() -> client.get(User.class, username).flatMap(user -> {
            var email = user.getSpec().getEmail();
            if (!user.getSpec().isEmailVerified() || StringUtils.isBlank(email)) {
                return Mono.error(new ServerWebInputException("Email is not verified."));
            }
            var verified = emailVerificationManager.verifyCode(username, email, code);
            if (!verified) {
                return Mono.error(EmailVerificationFailed::new);
            }
            // remove code when verified
            emailVerificationManager.removeCode(username, email);
            return Mono.empty();
        }));
    }

    Mono<Void> sendSecurityVerificationNotification(String username, String email) {
        var code = emailVerificationManager.generateCode(username, email);
        if (log.isDebugEnabled()) {
            log.debug("Generated security verification code for user '{}' and email '{}': {}", username, email, code);
        }
        var subscribeNotification = autoSubscribeVerificationEmailNotification(email);
        var interestReasonSubject = createInterestReason(email).getSubject();
        var emitReasonMono = reasonEmitter.emit(
                SECURITY_VERIFICATION_REASON_TYPE,
                builder -> builder.attribute("code", code)
                        .attribute("expirationAtMinutes", CODE_EXPIRATION_MINUTES)
                        .attribute("username", username)
                        .author(UserIdentity.of(username))
                        .subject(Reason.Subject.builder()
                                .apiVersion(interestReasonSubject.getApiVersion())
                                .kind(interestReasonSubject.getKind())
                                .name(interestReasonSubject.getName())
                                .title("安全验证：" + email)
                                .build()));
        return Mono.when(subscribeNotification).then(emitReasonMono);
    }
```

说明：`autoSubscribeVerificationEmailNotification` / `createInterestReason` 复用现有私有方法（订阅机制相同，仅 reasonType 不同）。

- [ ] **Step 5: 新增 ReasonType** `application/src/main/resources/extensions/notification.yaml`（在 `email-verification` ReasonType 之后追加）

```yaml
---
apiVersion: notification.halo.run/v1alpha1
kind: ReasonType
metadata:
  name: security-verification
  labels:
    halo.run/hidden: "true"
spec:
  displayName: "安全验证"
  description: "当你的账号正在进行安全验证时，会收到一条带有验证码的邮件。"
  properties:
    - name: username
      type: string
      description: "The username of the user."
    - name: code
      type: string
      description: "The verification code."
    - name: expirationAtMinutes
      type: string
      description: "The expiration minutes of the verification code, such as 10 minutes."
```

- [ ] **Step 6: 新增 NotificationTemplate** `application/src/main/resources/extensions/notification-templates.yaml`（在 `template-email-verification` 之后追加）

```yaml
---
apiVersion: notification.halo.run/v1alpha1
kind: NotificationTemplate
metadata:
  name: template-security-verification
spec:
  reasonSelector:
    reasonType: security-verification
    language: default
  template:
    title: "安全验证-[(${site.title})]"
    rawBody: |
      【[(${site.title})]】你的账号正在进行安全验证，验证码是：[(${code})]，请在 [(${expirationAtMinutes})] 分钟内完成验证。
    htmlBody: |
      <div class="notification-content">
        <div class="head">
          <p class="honorific" th:text="|${username} 你好：|"></p>
        </div>
        <div class="body">
          <p>你的账号正在进行安全验证，请使用下面的验证码完成验证。</p>
          <div class="verify-code" style="font-size:24px;line-height:24px;color:#333;">
            <b th:text="${code}"></b>
          </div>
          <p th:text="|验证码的有效期为 ${expirationAtMinutes} 分钟。|"></p>
          <p>如果这不是你的操作，请尽快修改你的密码。</p>
        </div>
      </div>
```

- [ ] **Step 7: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.core.user.service.impl.EmailVerificationServiceImplTest"`
Expected: PASS。

- [ ] **Step 8: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/core/user/service/EmailVerificationService.java \
  application/src/main/java/run/halo/app/core/user/service/impl/EmailVerificationServiceImpl.java \
  application/src/test/java/run/halo/app/core/user/service/impl/EmailVerificationServiceImplTest.java \
  application/src/main/resources/extensions/notification.yaml \
  application/src/main/resources/extensions/notification-templates.yaml
git commit -m "feat: add security verification email code service and notification"
```

---

### Task 3: `SecurityVerificationService` + `SecurityVerificationRequiredException`

**Files:**
- Create: `application/src/main/java/run/halo/app/security/verification/SecurityVerificationService.java`
- Create: `application/src/main/java/run/halo/app/security/verification/SecurityVerificationRequiredException.java`
- Test: `application/src/test/java/run/halo/app/security/verification/SecurityVerificationServiceTest.java`

**Interfaces:**
- Consumes: `User`（`getSpec().isEmailVerified()`、`getSpec().getTotpEncryptedSecret()`）、`TwoFactorUtils.getTwoFactorAuthSettings(User)`
- Produces:
- `SecurityVerificationService.isVerified(WebSession) → boolean`
- `SecurityVerificationService.markVerified(WebSession) → void`
- `SecurityVerificationService.isAvailable(User) → boolean`
- `SecurityVerificationRequiredException`（403，body 属性 `redirectURI = /security-verification`）——Task 6/7 使用

- [ ] **Step 1: 写失败测试** `SecurityVerificationServiceTest.java`

```java
package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;

class SecurityVerificationServiceTest {

    final SecurityVerificationService service = new SecurityVerificationService();

    WebSession session() {
        return MockServerWebExchange.builder(MockServerHttpRequest.get("/")).build()
                .getSession()
                .block();
    }

    User user(boolean emailVerified, String totpSecret) {
        var spec = new UserSpec();
        spec.setEmailVerified(emailVerified);
        spec.setTotpEncryptedSecret(totpSecret);
        var user = new User();
        user.setSpec(spec);
        user.setMetadata(new Metadata());
        return user;
    }

    @Test
    void shouldNotBeVerifiedInitially() {
        assertThat(service.isVerified(session())).isFalse();
    }

    @Test
    void shouldBeVerifiedAfterMarking() {
        var session = session();
        service.markVerified(session);
        assertThat(service.isVerified(session)).isTrue();
    }

    @Test
    void shouldExpireAfterTtl() {
        var session = session();
        session.getAttributes()
                .put(SecurityVerificationService.VERIFIED_AT_SESSION_KEY,
                        Instant.now().minus(SecurityVerificationService.VERIFICATION_TTL.plus(Duration.ofSeconds(1))));
        assertThat(service.isVerified(session)).isFalse();
    }

    @Test
    void shouldBeAvailableWhenEmailVerified() {
        assertThat(service.isAvailable(user(true, null))).isTrue();
    }

    @Test
    void shouldBeAvailableWhenTotpConfigured() {
        assertThat(service.isAvailable(user(false, "encrypted-secret"))).isTrue();
    }

    @Test
    void shouldNotBeAvailableWithoutAnyMethod() {
        assertThat(service.isAvailable(user(false, null))).isFalse();
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationServiceTest"`
Expected: 编译失败，类不存在。

- [ ] **Step 3: 实现** `SecurityVerificationService.java`

```java
package run.halo.app.security.verification;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import run.halo.app.core.extension.User;
import run.halo.app.security.authentication.twofactor.TwoFactorUtils;

/**
 * Session-scoped security verification (sudo mode) shared by sensitive operations.
 */
@Component
public class SecurityVerificationService {

    public static final String VERIFIED_AT_SESSION_KEY = "security-verification.verified-at";

    public static final Duration VERIFICATION_TTL = Duration.ofMinutes(30);

    /**
     * Whether the session has passed security verification within the TTL.
     */
    public boolean isVerified(WebSession session) {
        var verifiedAt = session.getAttribute(VERIFIED_AT_SESSION_KEY);
        return verifiedAt instanceof Instant instant && instant.plus(VERIFICATION_TTL).isAfter(Instant.now());
    }

    /**
     * Mark the session as security verified from now on.
     */
    public void markVerified(WebSession session) {
        session.getAttributes().put(VERIFIED_AT_SESSION_KEY, Instant.now());
    }

    /**
     * Whether the user has any security verification method (verified email or TOTP).
     */
    public boolean isAvailable(User user) {
        var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
        return settings.isEmailVerified() || settings.isTotpConfigured();
    }
}
```

- [ ] **Step 4: 实现** `SecurityVerificationRequiredException.java`

```java
package run.halo.app.security.verification;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a sensitive operation requires an unexpired security verification.
 */
public class SecurityVerificationRequiredException extends ResponseStatusException {

    private static final URI TYPE = URI.create("https://halo.run/probs/security-verification-required");

    public static final URI REDIRECT_LOCATION = URI.create("/security-verification");

    public SecurityVerificationRequiredException() {
        super(HttpStatus.FORBIDDEN, "Security verification required");
        setType(TYPE);
        getBody().setProperty("redirectURI", REDIRECT_LOCATION);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationServiceTest"`
Expected: PASS。

- [ ] **Step 6: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/verification/ \
  application/src/test/java/run/halo/app/security/verification/
git commit -m "feat: add security verification session service and exception"
```

---

### Task 4: 验证页模板

**Files:**
- Create: `application/src/main/resources/templates/security-verification.html`
- Create: `application/src/main/resources/templates/security-verification.properties`、`_en.properties`、`_es.properties`、`_zh_TW.properties`
- Create: `application/src/main/resources/templates/gateway_fragments/security-verification.html`
- Create: `application/src/main/resources/templates/gateway_fragments/security-verification.properties`、`_en.properties`、`_es.properties`、`_zh_TW.properties`

**Interfaces:**
- Consumes（render 模型，Task 5 提供）：`globalInfo`（Mono&lt;GlobalInfo&gt;）、`emailVerified`（boolean）、`totpConfigured`（boolean）、`redirect`（String）
- Produces: 模板 `security-verification` 及其 fragment（Task 5 渲染、Task 6 集成测试断言）

- [ ] **Step 1: 页面模板** `security-verification.html`

```html
<!DOCTYPE html>
<html
  xmlns:th="https://www.thymeleaf.org"
  th:replace="~{gateway_fragments/layout :: layout(title = |#{title} - ${site.title}|, head = ~{::head}, body = ~{::body})}"
>
  <th:block th:fragment="body">
    <div class="gateway-wrapper">
      <div th:replace="~{gateway_fragments/common::haloLogo}"></div>
      <div class="halo-form-wrapper">
        <h1 class="form-title" th:text="#{title}"></h1>
        <p class="form-description" th:text="#{subtitle}"></p>
        <form th:replace="~{gateway_fragments/security-verification::form}"></form>
      </div>
    </div>
  </th:block>

  <th:block th:fragment="head">
    <style>
      .security-verification-form .method-switcher {
        display: flex;
        margin-bottom: 1.25rem;
        border: 1px solid var(--color-border);
        border-radius: var(--radius);
        overflow: hidden;
      }
      .security-verification-form .method-tab {
        flex: 1;
        padding: 0.625rem;
        background: transparent;
        border: none;
        cursor: pointer;
        color: var(--color-text);
        font-size: var(--text-sm);
      }
      .security-verification-form .method-tab.active {
        background: var(--color-primary);
        color: #fff;
      }
      .security-verification-form .form-description {
        margin-bottom: 1.25rem;
        color: var(--color-text-secondary);
        font-size: var(--text-sm);
      }
    </style>
  </th:block>
</html>
```

- [ ] **Step 2: 表单 fragment** `gateway_fragments/security-verification.html`

```html
<form
  th:fragment="form"
  class="halo-form security-verification-form"
  th:action="@{/security-verification}"
  name="security-verification-form"
  id="security-verification-form"
  method="post"
>
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  <input type="hidden" name="redirect" th:value="${redirect}" />

  <div class="alert alert-error" role="alert" th:if="${param.error.size() > 0}">
    <strong
      th:if="${#strings.equals(param.error[0], 'rate-limit-exceeded')}"
      th:text="#{form.messages.rateLimitError}"
    ></strong>
    <strong
      th:unless="${#strings.equals(param.error[0], 'rate-limit-exceeded')}"
      th:text="#{form.messages.invalidError}"
    ></strong>
  </div>

  <div class="method-switcher" th:if="${emailVerified} and ${totpConfigured}">
    <button type="button" class="method-tab active" data-method="email" th:text="#{form.method.email}"></button>
    <button type="button" class="method-tab" data-method="totp" th:text="#{form.method.totp}"></button>
  </div>

  <div class="form-item" th:if="${emailVerified}" id="email-method">
    <label for="emailCode" th:text="#{form.emailCode.label}"></label>
    <div class="form-input-group">
      <div class="form-input">
        <input
          type="text"
          inputmode="numeric"
          id="emailCode"
          name="emailCode"
          autocomplete="one-time-code"
          pattern="\d{6}"
          maxlength="6"
          required
        />
      </div>
      <button id="emailCodeSendButton" type="button" th:text="#{form.emailCode.send}"></button>
    </div>
  </div>

  <div class="form-item" th:if="${totpConfigured}" id="totp-method">
    <label for="totpCode" th:text="#{form.totpCode.label}"></label>
    <div class="form-input">
      <input
        type="text"
        inputmode="numeric"
        id="totpCode"
        name="totpCode"
        autocomplete="one-time-code"
        pattern="\d{6}"
        maxlength="6"
        th:disabled="${emailVerified}"
        required
      />
    </div>
  </div>

  <div class="form-item">
    <button type="submit" th:text="#{form.submit}"></button>
  </div>
  <div class="form-item">
    <a th:href="@{/uc/profile}" class="cancel-link" th:text="#{form.cancel}"></a>
  </div>

  <script th:inline="javascript">
    document.addEventListener("DOMContentLoaded", function () {
      const headerName = /*[[${_csrf.headerName}]]*/ "";
      const token = /*[[${_csrf.token}]]*/ "";
      const form = document.getElementById("security-verification-form");
      const emailMethod = document.getElementById("email-method");
      const totpMethod = document.getElementById("totp-method");
      const tabs = Array.from(document.querySelectorAll(".method-tab"));

      function activateMethod(method) {
        tabs.forEach((tab) => {
          tab.classList.toggle("active", tab.dataset.method === method);
        });
        const useEmail = method === "email";
        emailMethod.style.display = useEmail ? "" : "none";
        totpMethod.style.display = useEmail ? "none" : "";
        emailMethod.querySelector("input").disabled = !useEmail;
        totpMethod.querySelector("input").disabled = useEmail;
      }

      tabs.forEach((tab) => {
        tab.addEventListener("click", function () {
          activateMethod(tab.dataset.method);
        });
      });

      const emailCodeSendButton = document.getElementById("emailCodeSendButton");
      if (emailCodeSendButton) {
        async function sendRequest() {
          const response = await fetch("/security-verification/email-code", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              [headerName]: token,
            },
          });

          if (!response.ok) {
            const json = await response.json();
            if (json.errors && json.errors.length) {
              throw new Error(json.errors[0]);
            }
            if (json.detail) {
              throw new Error(json.detail);
            }
            throw new Error(i18nResources.sendVerificationCodeFailed);
          }

          return response;
        }

        sendVerificationCode(emailCodeSendButton, sendRequest);
      }

      const totpInput = document.getElementById("totpCode");
      if (totpInput) {
        let submitting = false;
        totpInput.addEventListener("input", function (event) {
          if (submitting || event.isComposing || totpInput.disabled) {
            return;
          }
          if (/^\d{6}$/.test(totpInput.value)) {
            submitting = true;
            form.requestSubmit();
          }
        });
      }
    });
  </script>
</form>
```

说明：`sendVerificationCode(button, sendRequest)` 是 `/js/main.js` 的共享助手（60 秒倒计时、`i18nResources` 文案、Toast），`login_email-code.html` 同款用法。`_csrf` 由 Spring Security 的 CsrfWebFilter 注入模板上下文。

- [ ] **Step 3: 页面 properties**（`security-verification.properties` + 3 个语言变体）

`security-verification.properties`（默认，中文）：

```properties
title=安全验证
subtitle=为保障账号安全，请完成安全验证后继续操作
```

`security-verification_en.properties`：

```properties
title=Security verification
subtitle=For the security of your account, please complete the security verification to continue.
```

`security-verification_es.properties`：

```properties
title=Verificación de seguridad
subtitle=Para la seguridad de tu cuenta, completa la verificación de seguridad para continuar.
```

`security-verification_zh_TW.properties`：

```properties
title=安全驗證
subtitle=為保障帳號安全，請完成安全驗證後繼續操作
```

- [ ] **Step 4: fragment properties**（`gateway_fragments/security-verification.properties` + 3 个语言变体）

`gateway_fragments/security-verification.properties`（默认，中文）：

```properties
form.method.email=邮箱验证码
form.method.totp=TOTP 验证码
form.emailCode.label=邮箱验证码
form.emailCode.send=发送验证码
form.totpCode.label=TOTP 验证码
form.messages.invalidError=验证码不正确，请重新输入
form.messages.rateLimitError=请求过于频繁，请稍后再试
form.submit=验证
form.cancel=取消
```

`gateway_fragments/security-verification_en.properties`：

```properties
form.method.email=Email code
form.method.totp=TOTP code
form.emailCode.label=Email verification code
form.emailCode.send=Send code
form.totpCode.label=TOTP code
form.messages.invalidError=Invalid verification code. Please try again.
form.messages.rateLimitError=Too many requests. Please try again later.
form.submit=Verify
form.cancel=Cancel
```

`gateway_fragments/security-verification_es.properties`：

```properties
form.method.email=Código de correo electrónico
form.method.totp=Código TOTP
form.emailCode.label=Código de verificación de correo electrónico
form.emailCode.send=Enviar código
form.totpCode.label=Código TOTP
form.messages.invalidError=Código de verificación incorrecto. Inténtalo de nuevo.
form.messages.rateLimitError=Demasiadas solicitudes. Inténtalo más tarde.
form.submit=Verificar
form.cancel=Cancelar
```

`gateway_fragments/security-verification_zh_TW.properties`：

```properties
form.method.email=信箱驗證碼
form.method.totp=TOTP 驗證碼
form.emailCode.label=信箱驗證碼
form.emailCode.send=發送驗證碼
form.totpCode.label=TOTP 驗證碼
form.messages.invalidError=驗證碼不正確，請重新輸入
form.messages.rateLimitError=請求過於頻繁，請稍後再試
form.submit=驗證
form.cancel=取消
```

- [ ] **Step 5: 校验模板编译并提交**

Run: `./gradlew :application:processResources`
Expected: 成功（Thymeleaf 语法错误会在渲染时暴露，Task 5 集成测试覆盖渲染）。

```bash
git add application/src/main/resources/templates/security-verification.html \
  application/src/main/resources/templates/security-verification*.properties \
  application/src/main/resources/templates/gateway_fragments/security-verification.html \
  application/src/main/resources/templates/gateway_fragments/security-verification*.properties
git commit -m "feat: add security verification page templates"
```

---

### Task 5: `SecurityVerificationEndpoint` GET 渲染 + 授权规则

**Files:**
- Create: `application/src/main/java/run/halo/app/security/verification/SecurityVerificationEndpoint.java`
- Modify: `application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java`（Order 300 规则加路径）
- Test: `application/src/test/java/run/halo/app/security/verification/SecurityVerificationEndpointIntegrationTest.java`

**Interfaces:**
- Consumes: `UserService.getUser(String)`、`GlobalInfoService.getGlobalInfo()`、`TwoFactorUtils.getTwoFactorAuthSettings(User)`、`SecurityVerificationService.isAvailable(User)`（Task 3）
- Produces: `GET /security-verification`（渲染页面，注入 `globalInfo`/`emailVerified`/`totpConfigured`/`redirect`）、授权规则 `/security-verification/**` → authenticated

- [ ] **Step 1: 写失败集成测试** `SecurityVerificationEndpointIntegrationTest.java`

```java
package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityVerificationEndpointIntegrationTest {

    private static final String USERNAME = "faker";

    WebTestClient webClient;

    @Autowired
    WebTestClient baseWebClient;

    @MockitoBean
    UserService userService;

    @MockitoBean
    EmailVerificationService emailVerificationService;

    @MockitoBean
    TotpAuthService totpAuthService;

    @BeforeEach
    void setUp() {
        webClient = baseWebClient.mutateWith(springSecurity());
    }

    User user(boolean emailVerified, String totpEncryptedSecret) {
        var spec = new UserSpec();
        spec.setDisplayName("Faker");
        spec.setEmail("faker@halo.run");
        spec.setEmailVerified(emailVerified);
        spec.setTotpEncryptedSecret(totpEncryptedSecret);
        var user = new User();
        user.setSpec(spec);
        var metadata = new Metadata();
        metadata.setName(USERNAME);
        user.setMetadata(metadata);
        return user;
    }

    @Test
    void shouldRedirectToLoginWhenAnonymous() {
        webClient.get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/login?authentication_required");
    }

    @Test
    void shouldRenderPageWithEmailMethod() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        webClient.mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertThat(body).contains("security-verification-form");
                    assertThat(body).contains("emailCode");
                    assertThat(body).doesNotContain("method-switcher");
                });
    }

    @Test
    void shouldRenderPageWithMethodSwitcher() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, "encrypted-secret")));
        webClient.mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertThat(body).contains("method-switcher");
                    assertThat(body).contains("totpCode");
                });
    }

    @Test
    void shouldRedirectAwayWhenNoMethodAvailable() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, null)));
        webClient.mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification?redirect=/uc/profile")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenNoMethodAvailableAndNoRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, null)));
        webClient.mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }
}
```

注意：`webClient.mutateWith(mockUser(...))` 返回新 client，赋给局部变量使用（勿修改 `webClient` 字段本身）。`@MockitoBean TotpAuthService` 用于避免真实加密配置加载。

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationEndpointIntegrationTest"`
Expected: 404（端点不存在）。

- [ ] **Step 3: 实现端点** `SecurityVerificationEndpoint.java`

```java
package run.halo.app.security.verification;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authentication.twofactor.TwoFactorUtils;
import run.halo.app.security.authentication.twofactor.TotpVerificationService;

/**
 * Post-auth endpoint for the security verification page (sudo mode).
 *
 * @author JohnNiang
 * @since 2.26.0
 */
@Component
@RequiredArgsConstructor
class SecurityVerificationEndpoint {

    private static final String DEFAULT_REDIRECT = "/uc/profile";

    private final UserService userService;
    private final GlobalInfoService globalInfoService;
    private final EmailVerificationService emailVerificationService;
    private final TotpVerificationService totpVerificationService;
    private final SecurityVerificationService securityVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> securityVerificationEndpoints() {
        return RouterFunctions.route()
                .GET("/security-verification", this::renderVerificationPage)
                .POST("/security-verification", this::verifySecurityVerification)
                .POST("/security-verification/email-code", this::sendEmailCode)
                .before(HaloUtils.noCache())
                .build();
    }

    private Mono<ServerResponse> renderVerificationPage(ServerRequest request) {
        var redirect = safeRedirect(request.queryParam("redirect").orElse(DEFAULT_REDIRECT));
        return currentUser(request)
                .flatMap(user -> {
                    if (!securityVerificationService.isAvailable(user)) {
                        return redirectTo(redirect);
                    }
                    var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
                    var model = new HashMap<String, Object>();
                    model.put("globalInfo", globalInfoService.getGlobalInfo());
                    model.put("emailVerified", settings.isEmailVerified());
                    model.put("totpConfigured", settings.isTotpConfigured());
                    model.put("redirect", redirect);
                    return ServerResponse.ok().render("security-verification", model);
                });
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return currentUser(request)
                .flatMap(user -> emailVerificationService
                        .sendSecurityVerificationCode(user.getMetadata().getName())
                        .then(ServerResponse.accepted().build()));
    }

    private Mono<ServerResponse> verifySecurityVerification(ServerRequest request) {
        return request.formData()
                .flatMap(form -> {
                    var redirect = safeRedirect(form.getFirst("redirect"));
                    return currentUser(request)
                            .flatMap(user -> {
                                var username = user.getMetadata().getName();
                                var emailCode = form.getFirst("emailCode");
                                var totpCode = form.getFirst("totpCode");
                                var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
                                Mono<Void> verifyMono;
                                if (StringUtils.isNotBlank(totpCode)) {
                                    // Only accept TOTP when the user actually has TOTP configured,
                                    // otherwise TotpVerificationService.validate passes unconditionally.
                                    verifyMono = settings.isTotpConfigured()
                                            ? totpVerificationService.validate(user, totpCode)
                                            : Mono.error(new ServerWebInputException("TOTP is not configured."));
                                } else if (StringUtils.isNotBlank(emailCode)) {
                                    verifyMono = settings.isEmailVerified()
                                            ? emailVerificationService.verifySecurityVerificationCode(
                                                    username, emailCode)
                                            : Mono.error(new ServerWebInputException("Email is not verified."));
                                } else {
                                    verifyMono = Mono.error(new ServerWebInputException(
                                            "Verification code is required"));
                                }
                                return verifyMono
                                        .then(request.exchange().getSession())
                                        .doOnNext(securityVerificationService::markVerified)
                                        .then(redirectTo(redirect));
                            })
                            .onErrorResume(RequestNotPermitted.class,
                                    e -> redirectWithError(redirect, "rate-limit-exceeded"))
                            .onErrorResume(EmailVerificationFailed.class, e -> {
                                var error = "problemDetail.user.email.verify.maxAttempts"
                                        .equals(e.getDetailMessageCode())
                                                ? "rate-limit-exceeded"
                                                : "invalid-code";
                                return redirectWithError(redirect, error);
                            })
                            .onErrorResume(ServerWebInputException.class,
                                    e -> redirectWithError(redirect, "invalid-code"));
                });
    }

    private Mono<User> currentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null
                        && !(authentication instanceof AnonymousAuthenticationToken))
                .switchIfEmpty(Mono.error(AccessDeniedException::new))
                .map(Authentication::getName)
                .flatMap(userService::getUser);
    }

    private static String safeRedirect(String redirect) {
        if (StringUtils.hasText(redirect) && redirect.startsWith("/")
                && !redirect.startsWith("//") && !redirect.contains("\\")) {
            try {
                URI.create(redirect);
                return redirect;
            } catch (IllegalArgumentException e) {
                // fall through to default
            }
        }
        return DEFAULT_REDIRECT;
    }

    private static Mono<ServerResponse> redirectTo(String location) {
        return ServerResponse.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    private static Mono<ServerResponse> redirectWithError(String redirect, String error) {
        var location = UriComponentsBuilder.fromPath("/security-verification")
                .queryParam("error", error)
                .queryParam("redirect", redirect)
                .build()
                .toUri();
        return redirectTo(location.toString());
    }
}
```

需要补全的 import（除文件头已有的）：`io.github.resilience4j.ratelimiter.RequestNotPermitted`、`java.net.URI`、`org.springframework.http.HttpStatus`、`org.springframework.security.authentication.AnonymousAuthenticationToken`、`org.springframework.security.core.Authentication`、`org.springframework.security.core.context.ReactiveSecurityContextHolder`、`org.springframework.security.core.context.SecurityContext`、`org.springframework.web.server.ServerWebInputException`、`org.springframework.web.util.UriComponentsBuilder`、`run.halo.app.infra.exception.EmailVerificationFailed`。

> 注意：`verifySecurityVerification` 与限流（`rateLimiterRegistry` 字段）在 Task 6 中补全，本任务先实现 GET 渲染；`POST` 路由可以先返回未实现（本任务测试不涉及）。为了减少改动，直接按上方完整代码实现并在 Task 6 中补充限流部分亦可，但建议本任务只提交 GET + sendEmailCode（sendEmailCode 的限流在 Task 6 一并加）。

- [ ] **Step 4: 授权规则** `AuthorizationExchangeConfigurers.java`（`authenticatedAuthorizationConfigurer`，Order 300）

把：

```java
.pathMatchers("/complete-profile/**")
.authenticated()
```

改为：

```java
.pathMatchers("/complete-profile/**", "/security-verification/**")
.authenticated()
```

- [ ] **Step 5: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationEndpointIntegrationTest"`
Expected: PASS（4 个测试）。

- [ ] **Step 6: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/verification/SecurityVerificationEndpoint.java \
  application/src/main/java/run/halo/app/security/authorization/AuthorizationExchangeConfigurers.java \
  application/src/test/java/run/halo/app/security/verification/SecurityVerificationEndpointIntegrationTest.java
git commit -m "feat: render security verification page"
```

---

### Task 6: `SecurityVerificationEndpoint` POST 验证 + 发码 + 限流

**Files:**
- Modify: `application/src/main/java/run/halo/app/security/verification/SecurityVerificationEndpoint.java`（补全 `verifySecurityVerification`、`sendEmailCode`、限流方法）
- Test: `application/src/test/java/run/halo/app/security/verification/SecurityVerificationEndpointIntegrationTest.java`（追加测试）

**Interfaces:**
- Consumes: `TotpVerificationService.validate(User, String)`（Task 1）、`EmailVerificationService.sendSecurityVerificationCode/verifySecurityVerificationCode`（Task 2）、`SecurityVerificationService.markVerified(WebSession)`（Task 3）、限流配置 `totp-validation`（5 次/5 分钟）与 `send-login-email-code`（3 次/分钟）
- Produces: `POST /security-verification`（表单，成功 → 302 + 会话标记；失败 → 302 `?error=`）、`POST /security-verification/email-code`（JSON，202）

- [ ] **Step 1: 写失败测试**（追加到 `SecurityVerificationEndpointIntegrationTest.java`）

```java
    @Test
    void shouldSendEmailCodeWhenEmailVerified() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.sendSecurityVerificationCode(USERNAME)).thenReturn(Mono.empty());
        webClient.mutateWith(mockUser(USERNAME))
                .post()
                .uri("/security-verification/email-code")
                .exchange()
                .expectStatus()
                .isAccepted();
    }

    @Test
    void shouldVerifyWithEmailCodeAndRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        webClient.mutateWith(mockUser(USERNAME))
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldVerifyWithTotpCodeAndRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, "encrypted-secret")));
        when(totpAuthService.decryptSecret("encrypted-secret")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(true);
        webClient.mutateWith(mockUser(USERNAME))
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRedirectWithErrorWhenCodeInvalid() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, "encrypted-secret")));
        when(totpAuthService.decryptSecret("encrypted-secret")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(false);
        webClient.mutateWith(mockUser(USERNAME))
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=invalid-code&redirect=/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenRedirectIsExternal() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        webClient.mutateWith(mockUser(USERNAME))
                .post()
                .uri("/security-verification")
                .bodyValue("redirect=http://evil.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }
```

注意：表单 POST 需携带 CSRF——`mutateWith(csrf())` 会为表单附加 `_csrf` token，且表单 body 必须显式声明 `contentType(MediaType.APPLICATION_FORM_URLENCODED)`（既有先例：`OAuth2EmailCompletionFlowIntegrationTest` 第 255-260 行）。每个 POST 测试都需链式调用两个 mutator 并设置 contentType：

```java
webClient.mutateWith(mockUser(USERNAME)).mutateWith(csrf())
        .post()
        .uri("/security-verification?redirect=/uc/profile")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue("redirect=/uc/profile&emailCode=123456")
        ...
```

需要补全的 import：`static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf`、`org.springframework.http.MediaType`。

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationEndpointIntegrationTest"`
Expected: POST 路由未实现/限流未接入，至少 `shouldVerifyWithEmailCodeAndRedirect` 失败。

- [ ] **Step 3: 补全端点实现**（`SecurityVerificationEndpoint.java`）

`sendEmailCode` 改为带限流（按用户名，复用 `send-login-email-code` 配置 3 次/分钟）：

```java
    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return currentUser(request)
                .flatMap(user -> emailVerificationService
                        .sendSecurityVerificationCode(user.getMetadata().getName())
                        .transformDeferred(rateLimiterForSendingCode(user.getMetadata().getName()))
                        .then(ServerResponse.accepted().build()))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    private RateLimiterOperator<Void> rateLimiterForSendingCode(String username) {
        var rateLimiterKey = "send-security-verification-code-" + username;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-login-email-code");
        return RateLimiterOperator.of(rateLimiter);
    }
```

`verifySecurityVerification` 的 `verifyMono` 加限流（按会话，复用 `totp-validation` 配置 5 次/5 分钟，与登录 TOTP 同策略）：

```java
return verifyMono
        .transformDeferred(rateLimiterForVerification(request, username))
        .then(request.exchange().getSession())
        .doOnNext(securityVerificationService::markVerified)
        .then(redirectTo(redirect));
```

```java
private Function<Mono<Void>, Mono<Void>> rateLimiterForVerification(ServerRequest request, String username) {
    return mono -> request.exchange()
            .getSession()
            .map(WebSession::getId)
            // Fall back to a per-user key when no session can be derived
            // (e.g. mock test environment), so the rate limit still applies.
            .onErrorResume(throwable -> Mono.just(username))
            .flatMap(key -> {
                var rateLimiterKey = "totp-validation-" + key;
                var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "totp-validation");
                return mono.transformDeferred(RateLimiterOperator.of(rateLimiter));
            });
}
```

需要补全的 import：`io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator`、`java.util.function.Function`、`run.halo.app.infra.exception.RateLimitExceededException`。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.security.verification.SecurityVerificationEndpointIntegrationTest"`
Expected: PASS（全部测试，含 Task 5 的 4 个）。

- [ ] **Step 5: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/security/verification/SecurityVerificationEndpoint.java \
  application/src/test/java/run/halo/app/security/verification/SecurityVerificationEndpointIntegrationTest.java
git commit -m "feat: add security verification endpoints"
```

---

### Task 7: `UcUserEndpoint` 守卫 + `UcUserVo` 新字段

**Files:**
- Modify: `application/src/main/java/run/halo/app/core/endpoint/uc/UcUserEndpoint.java`
- Modify: `application/src/test/java/run/halo/app/core/endpoint/uc/UcUserEndpointTest.java`
- Modify（生成）：`ui/packages/api-client/src/models/uc-user-vo.ts` 等——Task 8 统一再生成，本任务只改后端

**Interfaces:**
- Consumes: `SecurityVerificationService.isVerified(WebSession)` / `isAvailable(User)`（Task 3）、`SecurityVerificationRequiredException`（Task 3）
- Produces: `UcUserVo.passwordChangeVerificationRequired`（boolean）、`PUT /users/-/password` 的 403 守卫（body 含 `redirectURI`）

- [ ] **Step 1: 写失败测试**（追加到 `UcUserEndpointTest.java`）

```java
    @Mock
    SecurityVerificationService securityVerificationService;

    User userWithEmailVerified(boolean passwordSet) {
        var user = createUser(passwordSet);
        user.getSpec().setEmailVerified(true);
        return user;
    }
```

新测试用例：

```java
    @Test
    void shouldGetPasswordChangeVerificationRequired() {
        when(userService.getUser("faker")).thenReturn(Mono.just(userWithEmailVerified(true)));
        when(securityVerificationService.isAvailable(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(false);
        webClient
                .mutate()
                .apply(mockAuthentication(new UsernamePasswordAuthenticationToken(
                        "faker", "password", createAuthorityList("ROLE_USER"))))
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
        when(securityVerificationService.isAvailable(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(true);
        webClient
                .mutate()
                .apply(mockAuthentication(new UsernamePasswordAuthenticationToken(
                        "faker", "password", createAuthorityList("ROLE_USER"))))
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
        when(securityVerificationService.isAvailable(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(false);
        webClient
                .mutate()
                .apply(mockAuthentication(new UsernamePasswordAuthenticationToken(
                        "faker", "password", createAuthorityList("ROLE_USER"))))
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
        when(securityVerificationService.isAvailable(any())).thenReturn(true);
        when(securityVerificationService.isVerified(any())).thenReturn(true);
        when(userService.confirmPassword("faker", "old-password")).thenReturn(Mono.just(true));
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockAuthentication(new UsernamePasswordAuthenticationToken(
                        "faker", "password", createAuthorityList("ROLE_USER"))))
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
        when(securityVerificationService.isAvailable(any())).thenReturn(true);
        when(userService.updateWithRawPassword("faker", "new-password")).thenReturn(Mono.just(createUser(true)));
        webClient
                .mutate()
                .apply(mockAuthentication(new UsernamePasswordAuthenticationToken(
                        "faker", "password", createAuthorityList("ROLE_USER"))))
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
```

需要补全的 import：`static org.mockito.ArgumentMatchers.any`、`org.springframework.security.authentication.UsernamePasswordAuthenticationToken`、`run.halo.app.security.verification.SecurityVerificationService`。检查文件头已有的 `createAuthorityList`/`mockAuthentication` import。

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew :application:test --tests "run.halo.app.core.endpoint.uc.UcUserEndpointTest"`
Expected: 编译失败（`passwordChangeVerificationRequired` / 守卫不存在）；注意现有测试可能因 `isAvailable` 默认 false 而仍通过，仅新测试失败。

- [ ] **Step 3: 实现** `UcUserEndpoint.java`

- 注入 `SecurityVerificationService`（`@RequiredArgsConstructor` 字段）：

```java
private final SecurityVerificationService securityVerificationService;
```

- `getCurrentUser` 改为传入 session 计算 VO：

```java
private Mono<ServerResponse> getCurrentUser(ServerRequest request) {
    return authenticated()
            .map(Authentication::getName)
            .flatMap(userService::getUser)
            .flatMap(user -> request.exchange().getSession().map(session -> toUserVo(user, session)))
            .flatMap(userVo -> ServerResponse.ok().bodyValue(userVo));
}
```

- `changeMyPassword` 在验证原密码之前加守卫（在 `flatMap(changeRequest -> userService.getUser(username).flatMap(user -> {` 内、`verifyPassword` 计算之前）：

```java
return requirePasswordChangeVerification(user, request)
        .then(verifyPassword
                .flatMap(ignored ->
                        userService.updateWithRawPassword(username, changeRequest.password()))
                .defaultIfEmpty(user)
                .map(this::toUserVo));
```

- 新增私有方法：

```java
private Mono<Void> requirePasswordChangeVerification(User user, ServerRequest request) {
    return request.exchange().getSession().flatMap(session -> {
        var passwordSet = StringUtils.hasText(user.getSpec().getPassword());
        if (passwordSet
                && securityVerificationService.isAvailable(user)
                && !securityVerificationService.isVerified(session)) {
            return Mono.error(new SecurityVerificationRequiredException());
        }
        return Mono.empty();
    });
}
```

- `toUserVo` 改为接收 session 并计算新字段：

```java
private UcUserVo toUserVo(User user, WebSession session) {
    var passwordSet = StringUtils.hasText(user.getSpec().getPassword());
    var verificationRequired = passwordSet
            && securityVerificationService.isAvailable(user)
            && !securityVerificationService.isVerified(session);
    return new UcUserVo(
            user.getMetadata().getName(),
            user.getSpec().getDisplayName(),
            user.getSpec().getAvatar(),
            passwordSet,
            verificationRequired);
}
```

> 注意：`toUserVo` 原签名被 `changeMyPassword` 的 `map(this::toUserVo)` 调用——该处需要 session，改为：
>
> ```java
> return requirePasswordChangeVerification(user, request)
>         .then(verifyPassword
>                 .flatMap(ignored ->
>                         userService.updateWithRawPassword(username, changeRequest.password()))
>                 .defaultIfEmpty(user)
>                 .flatMap(u -> request.exchange().getSession().map(session -> toUserVo(u, session))));
> ```
>
> - `UcUserVo` record 追加字段：
>
> ```java
> record UcUserVo(
> @Schema(requiredMode = REQUIRED) String name,
> String displayName,
> String avatar,
> @Schema(requiredMode = REQUIRED) boolean passwordSet,
> @Schema(requiredMode = REQUIRED) boolean passwordChangeVerificationRequired) {}
> ```
>
> - import 追加：`org.springframework.web.server.WebSession`、`run.halo.app.security.verification.SecurityVerificationRequiredException`、`run.halo.app.security.verification.SecurityVerificationService`。

- [ ] **Step 4: 运行全部相关测试确认通过**

Run: `./gradlew :application:test --tests "run.halo.app.core.endpoint.uc.UcUserEndpointTest"`
Expected: PASS（新旧全部）。

- [ ] **Step 5: 格式化并提交**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/core/endpoint/uc/UcUserEndpoint.java \
  application/src/test/java/run/halo/app/core/endpoint/uc/UcUserEndpointTest.java
git commit -m "feat: require security verification before changing password"
```

---

### Task 8: 前端接线 + 契约再生成

**Files:**
- Modify（生成）: `ui/packages/api-client/src/**`（`./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen`）
- Modify: `ui/uc-src/modules/profile/Profile.vue`（改密入口跳验证页、返回后自动打开弹窗）
- Modify: `ui/uc-src/modules/profile/components/PasswordChangeModal.vue`（403 时跳转 `redirectURI`）

**Interfaces:**
- Consumes: `GetMyUser` 响应新增 `passwordChangeVerificationRequired`（Task 7）、`UcUserVo` 类型
- Produces: 无（前端行为变更）

- [ ] **Step 1: 再生成 API 客户端**

Run: `./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen`
Expected: `ui/packages/api-client/src/models/uc-user-vo.ts` 出现 `passwordChangeVerificationRequired: boolean`。

- [ ] **Step 2: 改 `Profile.vue`**

script 部分（`fetchHasPassword` 扩展 + 新逻辑；`onMounted` 已有 block，把下面逻辑合并进现有 `onMounted`）：

```ts
import { useRoute } from "vue-router";
const route = useRoute();

const passwordChangeVerificationRequired = ref(false);

async function fetchPasswordState() {
  try {
    const { data } = await ucApiClient.user.currentUser.getMyUser();
    hasPassword.value = data.passwordSet;
    passwordChangeVerificationRequired.value = data.passwordChangeVerificationRequired;
  } catch (error) {
    console.error("Failed to get current user password status", error);
  }
}

function navigateToSecurityVerification() {
  const redirect = encodeURIComponent("/uc/profile?password-change=1");
  window.location.href = `/security-verification?redirect=${redirect}`;
}

async function handleChangePassword() {
  await fetchPasswordState();
  if (passwordChangeVerificationRequired.value) {
    navigateToSecurityVerification();
    return;
  }
  passwordChangeModal.value = true;
}
```

- 删除独立的 `fetchHasPassword` 定义与其调用，统一用 `fetchPasswordState()`。
- 在现有 `onMounted(async () => {...})` 开头追加：

```ts
if (route.query["password-change"] === "1") {
  await fetchPasswordState();
  if (passwordChangeVerificationRequired.value) {
    navigateToSecurityVerification();
  } else {
    passwordChangeModal.value = true;
  }
}
```

- 模板中改密入口（`<VDropdownItem @click="passwordChangeModal = true">`）改为：

```html
<VDropdownItem @click="handleChangePassword">
```

- [ ] **Step 3: 改 `PasswordChangeModal.vue`**（catch 块，403 + redirectURI 时跳转）

```ts
} catch (e) {
  console.error(e);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const redirectURI = (e as any)?.response?.data?.redirectURI;
  if (redirectURI) {
    window.location.href = redirectURI;
  }
}
```

- [ ] **Step 4: 前端校验**

Run: `pnpm -C ui typecheck && pnpm -C ui lint`
Expected: 无错误（lint 不得有 warning）。

- [ ] **Step 5: 提交**

```bash
git add ui/packages/api-client/src \
  ui/uc-src/modules/profile/Profile.vue \
  ui/uc-src/modules/profile/components/PasswordChangeModal.vue
git commit -m "feat: navigate to security verification before changing password in UC"
```

---

### Task 9: 全量回归

- [ ] **Step 1: 后端全量测试**

Run: `./gradlew :application:test`
Expected: 全部 PASS。

- [ ] **Step 2: 前端全量校验**

Run: `pnpm -C ui typecheck && pnpm -C ui lint && pnpm -C ui test:unit`
Expected: 全部通过。

- [ ] **Step 3: 手工验证（可选，`./gradlew :application:bootRun` + `pnpm -C ui dev`）**

1. 有密码 + 已验证邮箱的用户：点击"修改密码"→ 跳 `/security-verification` → 邮箱验证码 tab → 发送/输入 → 提交 → 回 `/uc/profile` 自动打开改密弹窗 → 改密成功。
2. 开启 TOTP 后：验证页出现切换器，切 TOTP tab 输入 6 位码自动提交。
3. 未验证邮箱且无 TOTP 的用户：点击"修改密码"直接打开弹窗（无验证页）。
4. 匿名访问 `/security-verification` → 302 到登录页。
5. 验证后 30 分钟内再次改密不要求重复验证。

## 自检记录（写计划时核对）

- **Spec 覆盖**：验证页主题模板（Task 4/5）、邮箱码/TOTP 任选其一（Task 4 模板 tab + Task 6 校验逻辑）、会话标记 TTL 30 分钟（Task 3）、仅已有密码需要验证（Task 7 守卫条件）、无能力回退（Task 5/7 的 `isAvailable` 条件）、redirect 站内校验（Task 6 `safeRedirect`）、`UcUserVo.passwordChangeVerificationRequired`（Task 7）、授权规则（Task 5）、通知原因/模板（Task 2）、TOTP 抽取复用（Task 1）、契约再生成（Task 8）。
- **占位符扫描**：所有代码步骤均给出完整代码；无 TBD/TODO。
- **类型一致性**：`TotpVerificationService.validate(User, String) → Mono<Void>`（Task 1 → 6）；`EmailVerificationService.sendSecurityVerificationCode(String) / verifySecurityVerificationCode(String, String) → Mono<Void>`（Task 2 → 6）；`SecurityVerificationService.isVerified(WebSession) / markVerified(WebSession) / isAvailable(User)`（Task 3 → 5/7）；`SecurityVerificationRequiredException.REDIRECT_LOCATION = /security-verification`（Task 3 → 7）；会话 key `security-verification.verified-at` 与 TTL 常量在 Task 3 定义，Task 3 测试引用同一常量。

