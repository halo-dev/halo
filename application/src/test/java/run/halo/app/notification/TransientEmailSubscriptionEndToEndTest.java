package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.DefaultControllerManager;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemState;
import run.halo.app.security.SuperAdminInitializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Opt-in HTTP → PostgreSQL → notification controllers → loopback SMTP tests, without mocked application beans. Set
 * HALO_NOTIFICATION_E2E_POSTGRES_URL to a disposable loopback PostgreSQL database named halo_notification_e2e, with
 * user halo_e2e and password from HALO_NOTIFICATION_E2E_POSTGRES_PASSWORD. Never point this at an existing site.
 */
@EnabledIfEnvironmentVariable(
        named = "HALO_NOTIFICATION_E2E_POSTGRES_URL",
        matches = "r2dbc:postgresql://127\\.0\\.0\\.1:[0-9]+/halo_notification_e2e")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.config.additional-location=file:src/main/resources/application.yaml",
            "logging.file.name=build/tmp/notification-e2e.log",
            "halo.extension.controller.disabled=false",
            "halo.security.basic-auth.disabled=false",
            "halo.notification.transient-email-cleanup-cron=-",
            "logging.level.run.halo.app=INFO",
            "logging.level.org.springframework.r2dbc=INFO"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
class TransientEmailSubscriptionEndToEndTest {
    private static final String PASSWORD = "E2ePass2026x!";
    private static final String USER_API = "/apis/api.console.halo.run/v1alpha1/users/-";

    @LocalServerPort
    int port;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    SuperAdminInitializer initializer;

    @Autowired
    NotifierConfigStore notifierConfig;

    @Autowired
    TransientEmailSubscriptionCleanupTask cleanup;

    @Autowired
    NotificationCenter notificationCenter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    SystemConfigFetcher systemConfig;

    @Autowired
    DefaultControllerManager controllers;

    LocalSmtpServer smtp;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws Exception {
        var workDir = Files.createTempDirectory("halo-notification-e2e-");
        registry.add("halo.work-dir", workDir::toString);
        registry.add("spring.r2dbc.url", () -> System.getenv("HALO_NOTIFICATION_E2E_POSTGRES_URL"));
        registry.add("spring.r2dbc.username", () -> "halo_e2e");
        registry.add("spring.r2dbc.password", () -> System.getenv("HALO_NOTIFICATION_E2E_POSTGRES_PASSWORD"));
        registry.add("spring.r2dbc.generate-unique-name", () -> false);
        registry.add("spring.sql.init.platform", () -> "postgresql");
    }

    @BeforeAll
    void initializeSiteAndLocalMail() throws Exception {
        smtp = new LocalSmtpServer();
        var config = JsonNodeFactory.instance
                .objectNode()
                .put("enable", true)
                .put("host", "127.0.0.1")
                .put("port", smtp.port())
                .put("encryption", "NONE")
                .put("username", "test")
                .put("password", "test")
                .put("sender", "halo@example.test")
                .put("displayName", "Halo E2E");
        notifierConfig.saveSenderConfig("default-email-notifier", config).block();
        SystemState.upsetSystemState(client, state -> state.setIsSetup(true)).block();
        Mono.defer(() -> client.get(ConfigMap.class, "system").flatMap(configMap -> {
                    var data = new HashMap<>(configMap.getData());
                    data.put("basic", "{\"title\":\"Halo E2E\"}");
                    configMap.setData(data);
                    return client.update(configMap);
                }))
                .retry(5)
                .block();
        until(() -> "Halo E2E".equals(systemConfig.getBasic().block().getTitle()));
        for (var username : List.of("e2e-main", "e2e-retry", "e2e-delayed-a", "e2e-delayed-b")) {
            initializer
                    .initialize(SuperAdminInitializer.InitializationParam.builder()
                            .username(username)
                            .password(PASSWORD)
                            .email(username + "@example.test")
                            .build())
                    .block();
            Mono.defer(() -> client.get(User.class, username).flatMap(user -> {
                        user.getSpec().setEmailVerified(true);
                        return client.update(user);
                    }))
                    .retry(5)
                    .block();
        }
    }

    @AfterAll
    void stopLocalMail() throws Exception {
        if (smtp != null) {
            smtp.close();
        }
    }

    @Test
    void allFourFlowsRemainUsableAfterSubscriptionsArePhysicallyCollected() throws Exception {
        var email = "verified@example.test";
        var admin = new Browser("e2e-main", PASSWORD);
        status(admin.json(USER_API + "/send-email-verification-code", Map.of("email", email)), 200);
        var verification = smtp.awaitMessage(email, "邮箱验证-");
        assertThat(verification.recipient()).isEqualTo(email);
        collect("email-verification", email);
        status(admin.json(USER_API + "/verify-email", Map.of("password", PASSWORD, "code", code(verification))), 200);
        assertThat(client.get(User.class, "e2e-main").block().getSpec().isEmailVerified())
                .isTrue();

        var browser = new Browser();
        browser.loadCsrf("/login");
        status(browser.json("/login/email-code/send", Map.of("email", email)), 202);
        var login = smtp.awaitMessage(email, "登录验证码-");
        assertThat(login.recipient()).isEqualTo(email);
        collect("login-email-code", email);
        var loggedIn = browser.form("/login/email-code", Map.of("email", email, "code", code(login)));
        status(loggedIn, 302);
        assertThat(loggedIn.headers().firstValue("location").orElseThrow()).doesNotContain("error");
        status(browser.get(USER_API), 200);

        status(browser.form("/sudo/code", Map.of("method", "email")), 204);
        var sudo = smtp.awaitMessage(email, "安全确认验证码-");
        assertThat(sudo.recipient()).isEqualTo(email);
        collect("sudo-email-code", email);
        status(browser.form("/sudo/confirm", Map.of("method", "email", "code", code(sudo))), 204);
        assertThat(jsonMapper
                        .readTree(browser.get("/sudo").body())
                        .path("active")
                        .asBoolean())
                .isTrue();

        var resetBrowser = new Browser();
        resetBrowser.loadCsrf("/password-reset/email");
        status(resetBrowser.form("/password-reset/email", Map.of("email", email)), 200);
        var reset = smtp.awaitMessage(email, "重置密码-");
        assertThat(reset.recipient()).isEqualTo(email);
        collect("reset-password-by-email", email);
        var link = Pattern.compile("https?://[^\\s<>]+/password-reset/email/[A-Za-z0-9]+")
                .matcher(reset.text());
        assertThat(link.find()).isTrue();
        var resetPath = URI.create(link.group()).getPath();
        resetBrowser.loadCsrf(resetPath);
        var newPassword = "E2eNewPass2026x!";
        var changed = resetBrowser.form(resetPath, Map.of("password", newPassword, "confirmPassword", newPassword));
        status(changed, 302);
        assertThat(changed.headers().firstValue("location").orElseThrow()).isEqualTo("/login?password_reset");
        assertThat(passwordEncoder.matches(
                        newPassword,
                        client.get(User.class, "e2e-main").block().getSpec().getPassword()))
                .isTrue();
        status(new Browser("e2e-main", newPassword).get(USER_API), 200);

        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        var reason = new Subscription.InterestReason();
        reason.setReasonType("someone-replied-to-you");
        reason.setSubject(Subscription.ReasonSubject.builder()
                .apiVersion("User")
                .kind("User")
                .name(subscriber.getName())
                .build());
        var permanent = notificationCenter.subscribe(subscriber, reason).block();
        cleanup.cleanUp();
        assertThat(client.get(Subscription.class, permanent.getMetadata().getName())
                        .block()
                        .getMetadata()
                        .getDeletionTimestamp())
                .isNull();
    }

    @Test
    void smtpFailureRetriesSuccessfullyAfterSubscriptionGc() throws Exception {
        var browser = new Browser("e2e-retry", PASSWORD);
        var email = "retry@example.test";
        var rejected = smtp.rejectNextDelivery(email);
        try {
            status(browser.json(USER_API + "/send-email-verification-code", Map.of("email", email)), 200);
            var firstAttempt = rejected.awaitAttempt();
            assertThat(firstAttempt.recipient()).isEqualTo(email);
            collect("email-verification", email);
            rejected.release();
            var retry = smtp.awaitMessage(email, "邮箱验证-");
            assertThat(retry.recipient()).isEqualTo(email);
            assertThat(code(retry)).isEqualTo(code(firstAttempt));
            status(browser.json(USER_API + "/verify-email", Map.of("password", PASSWORD, "code", code(retry))), 200);
        } finally {
            rejected.release();
        }
    }

    @Test
    void delayedReasonsProtectARebuiltSubscriptionUntilConsumptionResumes() throws Exception {
        // Stop the real consumers to create a deterministic backlog across subscription replacement.
        controllers.stop();
        var email = "delayed@example.test";
        try {
            status(
                    new Browser("e2e-delayed-a", PASSWORD)
                            .json(USER_API + "/send-email-verification-code", Map.of("email", email)),
                    200);
            until(() -> pendingReasons("email-verification", email) == 1);
            var oldName = subscriptions("email-verification", email).getFirst();
            cleanup.cleanUp();
            assertThat(subscriptions("email-verification", email)).containsExactly(oldName);
            status(
                    new Browser("e2e-delayed-b", PASSWORD)
                            .json(USER_API + "/send-email-verification-code", Map.of("email", email)),
                    200);
            until(() -> pendingReasons("email-verification", email) == 2);
            var newName = subscriptions("email-verification", email).getFirst();
            assertThat(newName).isNotEqualTo(oldName);
            cleanup.cleanUp();
            assertThat(subscriptions("email-verification", email)).containsExactly(newName);
        } finally {
            controllers.start();
        }
        assertThat(smtp.awaitMessage(email, "邮箱验证-").recipient()).isEqualTo(email);
        assertThat(smtp.awaitMessage(email, "邮箱验证-").recipient()).isEqualTo(email);
        collect("email-verification", email);
    }

    private void collect(String reasonType, String email) {
        until(() -> pendingReasons(reasonType, email) == 0);
        var names = subscriptions(reasonType, email);
        assertThat(names).isNotEmpty();
        cleanup.cleanUp();
        until(() -> names.stream()
                .allMatch(name ->
                        client.fetch(Subscription.class, name).blockOptional().isEmpty()));
        assertThat(subscriptions(reasonType, email)).isEmpty();
    }

    private List<String> subscriptions(String reasonType, String email) {
        return client.listAllNames(
                        Subscription.class,
                        ListOptions.builder()
                                .andQuery(equal("spec.reason.reasonType", reasonType))
                                .andQuery(equal(
                                        "spec.subscriber",
                                        UserIdentity.anonymousWithEmail(email).name()))
                                .andQuery(isNull("metadata.deletionTimestamp"))
                                .build(),
                        Sort.unsorted())
                .collectList()
                .block();
    }

    private long pendingReasons(String reasonType, String email) {
        return client.countBy(
                        Reason.class,
                        ListOptions.builder()
                                .andQuery(equal("spec.reasonType", reasonType))
                                .andQuery(equal(
                                        "spec.subject",
                                        "User#User/"
                                                + UserIdentity.anonymousWithEmail(email)
                                                        .name()))
                                .andQuery(isNull("metadata.deletionTimestamp"))
                                .build())
                .block();
    }

    private static String code(LocalSmtpServer.Message message) throws Exception {
        var matcher = Pattern.compile("(?<![0-9])[0-9]{6}(?![0-9])").matcher(message.text());
        assertThat(matcher.find()).isTrue();
        return matcher.group();
    }

    private static void until(BooleanSupplier condition) {
        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(50))
                .until(condition::getAsBoolean);
    }

    private static void status(HttpResponse<String> response, int expected) {
        assertThat(response.statusCode())
                .as("HTTP response: %s", response.body())
                .isEqualTo(expected);
    }

    private final class Browser {
        private final HttpClient http = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        private final String authorization;
        private String csrf;

        Browser() {
            authorization = null;
        }

        Browser(String username, String password) {
            authorization = "Basic "
                    + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        }

        void loadCsrf(String path) throws Exception {
            var response = get(path);
            status(response, 200);
            var input = Jsoup.parse(response.body()).selectFirst("input[name=_csrf]");
            assertThat(input).as("CSRF form in %s", path).isNotNull();
            csrf = input.attr("value");
        }

        HttpResponse<String> get(String path) throws Exception {
            return send(path, null, null);
        }

        HttpResponse<String> json(String path, Map<String, String> values) throws Exception {
            return send(path, "application/json", jsonMapper.writeValueAsString(values));
        }

        HttpResponse<String> form(String path, Map<String, String> values) throws Exception {
            var form = new HashMap<>(values);
            if (csrf != null) {
                form.put("_csrf", csrf);
            }
            var body = form.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            return send(path, "application/x-www-form-urlencoded", body);
        }

        private HttpResponse<String> send(String path, String contentType, String body) throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(20));
            if (authorization != null) {
                request.header("Authorization", authorization);
            }
            if (body != null) {
                request.header("Content-Type", contentType).POST(HttpRequest.BodyPublishers.ofString(body));
                if (csrf != null) {
                    request.header("X-XSRF-TOKEN", csrf);
                }
            }
            return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
