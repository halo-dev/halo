package run.halo.app.security.device;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.infra.utils.IpAddressUtils.getClientIp;
import static run.halo.app.security.authentication.rememberme.PersistentTokenBasedRememberMeServices.REMEMBER_ME_SERIES_REQUEST_NAME;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Device;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.infra.utils.UserAgentUtils;
import run.halo.app.security.authentication.rememberme.PersistentRememberMeTokenRepository;
import ua_parser.UserAgent;

@Slf4j
@Component
@RequiredArgsConstructor
class DeviceServiceImpl implements DeviceService {
    private final ReactiveExtensionClient client;
    private final DeviceCookieResolver deviceCookieResolver;
    private final ReactiveSessionRepository<?> sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PersistentRememberMeTokenRepository rememberMeTokenRepository;
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

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
            .mapNotNull(SecurityContext::getAuthentication)
            .filter(trustResolver::isAuthenticated)
            .map(Principal::getName)
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
                .flatMap(device -> {
                    if (!Objects.equals(username, device.getSpec().getPrincipalName())) {
                        log.debug(
                            "Principal name mismatch for device {}, expected {}, actual {}, "
                                + "revoking device",
                            deviceId, username, device.getSpec().getPrincipalName());
                        return doRevoke(device).then(Mono.empty());
                    }
                    return updateFunction.apply(device).flatMap(client::update);
                })
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
        return updateWithRetry(deviceIdCookie.getValue(), principalName, existingDevice -> {
            var userAgent = getUserAgent(exchange);
            var deviceUa = existingDevice.getSpec().getUserAgent();
            if (!Objects.equals(deviceUa, userAgent)) {
                // User agent changed, create a new device
                log.debug(
                    "User agent changed for device {}, expected {}, actual {}, revoking device",
                    existingDevice.getMetadata().getName(), deviceUa, userAgent);
                return doRevoke(existingDevice).then(Mono.empty());
            }
            var sessionId = existingDevice.getSpec().getSessionId();
            return exchange.getSession()
                .delayUntil(session -> {
                    if (session.getId().equals(sessionId)) {
                        return Mono.empty();
                    }
                    log.debug("Session ID changed for device {}, deleting old session {}",
                        existingDevice.getMetadata().getName(), sessionId);
                    return sessionRepository.deleteById(sessionId);
                })
                .doOnNext(session -> {
                    existingDevice.getSpec().setSessionId(session.getId());
                    existingDevice.getSpec().setLastAccessedTime(session.getLastAccessTime());
                    existingDevice.getSpec().setLastAuthenticatedTime(Instant.now());
                    existingDevice.getSpec().setRememberMeSeriesId(
                        exchange.getAttribute(REMEMBER_ME_SERIES_REQUEST_NAME)
                    );
                })
                .thenReturn(existingDevice);
        });
    }

    @Override
    public Mono<Void> revoke(String principalName, String deviceId) {
        return client.fetch(Device.class, deviceId)
            .filter(device -> device.getSpec().getPrincipalName().equals(principalName))
            .delayUntil(this::doRevoke)
            .then();
    }

    @Override
    public Mono<Void> revoke(String username) {
        var listOptions = ListOptions.builder()
            .andQuery(Queries.equal("spec.principalName", username))
            .build();
        return client.listAll(Device.class, listOptions, defaultSort())
            .delayUntil(this::doRevoke)
            .then();
    }

    @Override
    public Mono<Device> resolveCurrentDevice(ServerWebExchange exchange) {
        var deviceIdCookie = deviceCookieResolver.resolveCookie(exchange);
        if (deviceIdCookie == null) {
            return Mono.empty();
        }
        var deviceId = deviceIdCookie.getValue();
        return client.fetch(Device.class, deviceId)
            .filter(Predicate.not(ExtensionUtil::isDeleted));
    }

    private Mono<Void> doRevoke(Device device) {
        return removeRememberMeToken(device)
            .then(sessionRepository.deleteById(device.getSpec().getSessionId()))
            .then(client.delete(device))
            .then();
    }

    private Mono<Void> removeRememberMeToken(Device device) {
        var seriesId = device.getSpec().getRememberMeSeriesId();
        if (StringUtils.isBlank(seriesId)) {
            return Mono.empty();
        }
        log.debug("Removing remember-me token for seriesId: {} for device {}",
            seriesId, device.getMetadata().getName());
        return rememberMeTokenRepository.removeToken(seriesId);
    }

    Mono<Device> createDevice(ServerWebExchange exchange, Authentication authentication) {
        Assert.notNull(authentication, "Authentication must not be null.");
        return Mono.fromSupplier(
                () -> {
                    var device = new Device();
                    device.setMetadata(new Metadata());
                    device.getMetadata().setName(generateDeviceId());

                    var userAgent = getUserAgent(exchange);
                    var deviceInfo = DeviceInfo.parse(userAgent);
                    device.setSpec(new Device.Spec()
                        .setUserAgent(userAgent)
                        .setPrincipalName(authentication.getName())
                        .setLastAuthenticatedTime(Instant.now())
                        .setIpAddress(getClientIp(exchange.getRequest()))
                        .setRememberMeSeriesId(
                            exchange.getAttribute(REMEMBER_ME_SERIES_REQUEST_NAME)
                        )
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

    @Nullable
    private static String getUserAgent(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);
    }

    record DeviceInfo(String browser, String os) {

        public static DeviceInfo parse(String agentString) {
            var client = UserAgentUtils.parse(agentString);
            UserAgent ua = client.userAgent;
            var browserVersion = Stream.of(ua.major, ua.minor, ua.patch)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("."));
            var browserString = ua.family + (browserVersion.isEmpty() ? "" : " " + browserVersion);

            var os = client.os;
            var osVersion = Stream.of(os.major, os.minor, os.patch, os.patchMinor)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("."));
            var osString = os.family + (osVersion.isEmpty() ? "" : " " + osVersion);
            return new DeviceInfo(browserString, osString);
        }

    }
}
