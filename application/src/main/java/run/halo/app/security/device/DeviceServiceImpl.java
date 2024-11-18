package run.halo.app.security.device;

import static run.halo.app.infra.utils.IpAddressUtils.getClientIp;
import static run.halo.app.security.authentication.rememberme.PersistentTokenBasedRememberMeServices.REMEMBER_ME_SERIES_REQUEST_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Device;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authentication.rememberme.PersistentRememberMeTokenRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final ReactiveExtensionClient client;
    private final DeviceCookieResolver deviceCookieResolver;
    private final ReactiveSessionRepository<?> sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PersistentRememberMeTokenRepository rememberMeTokenRepository;

    @Override
    public Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication authentication) {
        return updateExistingDevice(exchange, authentication)
            .switchIfEmpty(createDevice(exchange, authentication)
                .flatMap(client::create)
                .doOnNext(device -> {
                    deviceCookieResolver.setCookie(exchange, device.getMetadata().getName());
                    eventPublisher.publishEvent(new NewDeviceLoginEvent(this, device));
                })
            )
            .then();
    }

    @Override
    public Mono<Void> changeSessionId(ServerWebExchange exchange) {
        var deviceIdCookie = deviceCookieResolver.resolveCookie(exchange);
        if (deviceIdCookie == null) {
            return Mono.empty();
        }
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> context.getAuthentication().getName())
            .flatMap(username -> {
                var deviceId = deviceIdCookie.getValue();
                return updateWithRetry(deviceId, username, device -> {
                    var oldSessionId = device.getSpec().getSessionId();
                    return exchange.getSession()
                        .filter(session -> !session.getId().equals(oldSessionId))
                        .flatMap(session -> {
                            device.getSpec().setSessionId(session.getId());
                            device.getSpec().setLastAccessedTime(session.getLastAccessTime());
                            return sessionRepository.deleteById(oldSessionId);
                        })
                        .thenReturn(device);
                }).then();
            });
    }

    private Mono<Device> updateWithRetry(String deviceId, String username,
        Function<Device, Mono<Device>> updateFunction) {
        return Mono.defer(() -> client.fetch(Device.class, deviceId)
                .filter(device -> device.getSpec().getPrincipalName().equals(username))
                .flatMap(updateFunction)
                .flatMap(client::update)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Device> updateExistingDevice(ServerWebExchange exchange,
        Authentication authentication) {
        var deviceIdCookie = deviceCookieResolver.resolveCookie(exchange);
        if (deviceIdCookie == null) {
            return Mono.empty();
        }
        var principalName = authentication.getName();
        return updateWithRetry(deviceIdCookie.getValue(), principalName,
            (Device existingDevice) -> {
                var sessionId = existingDevice.getSpec().getSessionId();
                return exchange.getSession()
                    .flatMap(session -> {
                        var userAgent =
                            exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);
                        var deviceUa = existingDevice.getSpec().getUserAgent();
                        if (!StringUtils.equals(deviceUa, userAgent)) {
                            // User agent changed, create a new device
                            return Mono.empty();
                        }
                        return Mono.just(session);
                    })
                    .flatMap(session -> {
                        if (session.getId().equals(sessionId)) {
                            return Mono.just(session);
                        }
                        return sessionRepository.deleteById(sessionId).thenReturn(session);
                    })
                    .map(session -> {
                        existingDevice.getSpec().setSessionId(session.getId());
                        existingDevice.getSpec().setLastAccessedTime(session.getLastAccessTime());
                        existingDevice.getSpec().setLastAuthenticatedTime(Instant.now());
                        return existingDevice;
                    })
                    .flatMap(this::removeRememberMeToken);
            });
    }

    @Override
    public Mono<Void> revoke(String principalName, String deviceId) {
        return client.fetch(Device.class, deviceId)
            .filter(device -> device.getSpec().getPrincipalName().equals(principalName))
            .flatMap(this::removeRememberMeToken)
            .flatMap(client::delete)
            .flatMap(revoked -> sessionRepository.deleteById(revoked.getSpec().getSessionId()));
    }

    private Mono<Device> removeRememberMeToken(Device device) {
        var seriesId = device.getSpec().getRememberMeSeriesId();
        if (StringUtils.isBlank(seriesId)) {
            return Mono.just(device);
        }
        log.debug("Removing remember-me token for seriesId: {}", seriesId);
        return rememberMeTokenRepository.removeToken(seriesId)
            .thenReturn(device);
    }

    Mono<Device> createDevice(ServerWebExchange exchange, Authentication authentication) {
        Assert.notNull(authentication, "Authentication must not be null.");
        return Mono.fromSupplier(
                () -> {
                    var device = new Device();
                    device.setMetadata(new Metadata());
                    device.getMetadata().setName(generateDeviceId());

                    var userAgent =
                        exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);
                    var deviceInfo = DeviceInfo.parse(userAgent);
                    device.setSpec(new Device.Spec()
                        .setUserAgent(userAgent)
                        .setPrincipalName(authentication.getName())
                        .setLastAuthenticatedTime(Instant.now())
                        .setIpAddress(getClientIp(exchange.getRequest()))
                        .setRememberMeSeriesId(
                            exchange.getAttribute(REMEMBER_ME_SERIES_REQUEST_NAME))
                    );
                    device.getStatus()
                        .setOs(deviceInfo.os())
                        .setBrowser(deviceInfo.browser());
                    return device;
                })
            .flatMap(device -> exchange.getSession()
                .doOnNext(session -> {
                    device.getSpec().setSessionId(session.getId());
                    device.getSpec().setLastAccessedTime(session.getLastAccessTime());
                })
                .thenReturn(device)
            );
    }

    String generateDeviceId() {
        return UUID.randomUUID().toString()
            .replace("-", "").toLowerCase();
    }

    record DeviceInfo(String browser, String os) {
        static final String UNKNOWN = "Unknown";
        static final Pattern BROWSER_REGEX =
            Pattern.compile("(MSIE|Trident|Edge|Edg|OPR|Opera|Chrome|Safari|Firefox"
                    + "|FxiOS|SamsungBrowser|UCBrowser|UCWEB|CriOS|Silk|Raven\\|Raven\\|)",
                Pattern.CASE_INSENSITIVE);
        static final Pattern BROWSER_VERSION_REGEX =
            Pattern.compile("(?:version/|chrome/|firefox/|safari/|msie "
                    + "|rv:|opr/|edg/|ucbrowser/|samsungbrowser/|crios/|silk/)(\\d+\\.\\d+)",
                Pattern.CASE_INSENSITIVE);

        static final Pattern OS_REGEX =
            Pattern.compile(
                "(Windows NT|Mac OS X|Android|Linux|iPhone|iPad|Windows Phone|OpenHarmony)");
        static final Pattern[] osRegexes = {
            Pattern.compile("Windows NT (\\d+\\.\\d+)"),
            Pattern.compile("Mac OS X (\\d+[\\._]\\d+([\\._]\\d+)?)"),
            Pattern.compile("iPhone OS (\\d+_\\d+(_\\d+)?)"),
            Pattern.compile("Android (\\d+\\.\\d+(\\.\\d+)?)"),
            Pattern.compile("OpenHarmony (\\d+\\.\\d+(\\.\\d+)?)")
        };

        public static DeviceInfo parse(String userAgent) {
            return new DeviceInfo(concat(parseBrowser(userAgent).name(),
                parseBrowser(userAgent).version()),
                concat(parseOperatingSystem(userAgent).name(),
                    parseOperatingSystem(userAgent).version())
            );
        }

        private static Pair parseBrowser(String userAgent) {
            Matcher matcher = BROWSER_REGEX.matcher(userAgent);
            if (matcher.find()) {
                String browserName = matcher.group(1);
                matcher = BROWSER_VERSION_REGEX.matcher(userAgent);

                if (matcher.find()) {
                    String browserVersion = matcher.group(1);
                    return new Pair(browserName, browserVersion);
                } else {
                    return new Pair(browserName, null);
                }
            } else {
                return new Pair(UNKNOWN, null);
            }
        }

        record Pair(String name, String version) {
        }

        private static Pair parseOperatingSystem(String userAgent) {
            Matcher matcher = OS_REGEX.matcher(userAgent);
            var osName = UNKNOWN;
            if (matcher.find()) {
                osName = matcher.group(1);
            }
            var osVersion = parseOsVersion(userAgent);
            return new Pair(osName, osVersion);
        }

        private static String parseOsVersion(String userAgent) {
            for (Pattern pattern : osRegexes) {
                Matcher matcher = pattern.matcher(userAgent);
                if (matcher.find()) {
                    return matcher.group(1).replace("_", ".");
                }
            }
            return "";
        }

        private static String concat(String name, String version) {
            return StringUtils.isBlank(version) ? name : name + " " + version;
        }
    }
}
