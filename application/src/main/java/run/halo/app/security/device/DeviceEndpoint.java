package run.halo.app.security.device;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.security.Principal;
import java.util.Comparator;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Device;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Device endpoint for user profile,every user can only manage their own devices.
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
@RequiredArgsConstructor
public class DeviceEndpoint implements CustomEndpoint {
    private final ReactiveExtensionClient client;
    private final ReactiveFindByIndexNameSessionRepository<?> sessionRepository;
    private final DeviceService deviceService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "DeviceV1alpha1Uc";
        return SpringdocRouteBuilder.route()
            .GET("devices", this::listDevices,
                builder -> builder.operationId("ListDevices")
                    .description("List all user devices")
                    .tag(tag)
                    .response(responseBuilder().implementationArray(DeviceDto.class))
            )
            .DELETE("devices/{deviceId}", this::revokeDevice, builder -> builder
                .operationId("RevokeDevice")
                .description("Revoke a own device")
                .tag(tag)
                .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("deviceId")
                    .description("Device ID")
                    .required(true)
                )
                .response(responseBuilder()
                    .responseCode(String.valueOf(HttpStatus.NO_CONTENT))
                )
            )
            .build();
    }

    private Mono<ServerResponse> revokeDevice(ServerRequest request) {
        final var deviceId = request.pathVariable("deviceId");
        return principalName()
            .flatMap(principalName -> deviceService.revoke(principalName, deviceId))
            .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> listDevices(ServerRequest request) {
        return getRequestContext(request)
            .flatMapMany(context -> {
                var listOptions = new ListOptions();
                var query = equal("spec.principalName", context.username());
                listOptions.setFieldSelector(FieldSelector.of(query));
                return client.listAll(Device.class, listOptions,
                        Sort.by("metadata.creationTimestamp"))
                    .map(device -> {
                        var sessionId = device.getSpec().getSessionId();
                        var session = context.sessionMap().get(sessionId);
                        if (session != null) {
                            device.getSpec().setLastAccessedTime(session.getLastAccessedTime());
                        }
                        return new DeviceDto()
                            .setDevice(device)
                            .setCurrentDevice(context.sessionId().equals(sessionId))
                            .setActive(session != null && !session.isExpired());
                    })
                    .sort(deviceDtoComparator());
            })
            .collectList()
            .flatMap(deviceDto -> ServerResponse.ok().bodyValue(deviceDto));
    }

    Comparator<DeviceDto> deviceDtoComparator() {
        return Comparator.comparing(DeviceDto::isCurrentDevice)
            .thenComparing(DeviceDto::isActive)
            .thenComparing(DeviceDto::getDevice, Comparator.comparing(device -> {
                var accessedTime = device.getSpec().getLastAccessedTime();
                return accessedTime == null ? device.getMetadata().getCreationTimestamp()
                    : accessedTime;
            }))
            .reversed();
    }

    private Mono<RequestContext> getRequestContext(ServerRequest request) {
        return principalName()
            .flatMap(principalName -> {
                var builder = RequestContext.builder()
                    .sessionMap(Map.of())
                    .username(principalName);
                var sessionMapMono = sessionRepository.findByPrincipalName(principalName)
                    .doOnNext(builder::sessionMap);
                var sessionMono = request.exchange().getSession()
                    .doOnNext(session -> builder.sessionId(session.getId()));
                return Mono.when(sessionMapMono, sessionMono)
                    .then(Mono.fromSupplier(builder::build));
            });
    }

    @Builder
    record RequestContext(String username, String sessionId,
                          Map<String, ? extends Session> sessionMap) {
    }

    Mono<String> principalName() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    @Data
    @Accessors(chain = true)
    @Schema(name = "UserDevice")
    static class DeviceDto {
        @Schema(requiredMode = REQUIRED)
        private Device device;

        @Schema(requiredMode = REQUIRED)
        boolean currentDevice;

        @Schema(requiredMode = REQUIRED)
        boolean active;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.security.halo.run/v1alpha1");
    }
}
