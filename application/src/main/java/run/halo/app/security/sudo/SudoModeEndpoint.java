package run.halo.app.security.sudo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * UC endpoints for sudo confirmation.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Component
class SudoModeEndpoint implements CustomEndpoint {

    private final SudoService sudoService;

    SudoModeEndpoint(SudoService sudoService) {
        this.sudoService = sudoService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "SudoV1alpha1Uc";
        return route().nest(
                        path("/authentications/sudo"),
                        () -> route().GET(
                                        this::getStatus,
                                        builder -> builder.operationId("GetSudoStatus")
                                                .tag(tag)
                                                .description("Get sudo confirmation status and available methods.")
                                                .response(responseBuilder().implementation(SudoStatus.class)))
                                .POST(
                                        "/code",
                                        this::sendCode,
                                        builder -> builder.operationId("SendSudoCode")
                                                .tag(tag)
                                                .description("Send a one-time sudo verification code.")
                                                .requestBody(requestBodyBuilder()
                                                        .required(true)
                                                        .implementation(SudoCodeRequest.class))
                                                .response(responseBuilder().responseCode("204")))
                                .POST(
                                        "/confirm",
                                        this::confirm,
                                        builder -> builder.operationId("ConfirmSudo")
                                                .tag(tag)
                                                .description("Confirm sudo with a verification method and code.")
                                                .requestBody(requestBodyBuilder()
                                                        .required(true)
                                                        .implementation(SudoConfirmRequest.class))
                                                .response(responseBuilder().responseCode("204")))
                                .build())
                .build();
    }

    private Mono<ServerResponse> getStatus(ServerRequest request) {
        return sudoService
                .status(request.exchange())
                .flatMap(status -> ServerResponse.ok().bodyValue(status));
    }

    private Mono<ServerResponse> sendCode(ServerRequest request) {
        return request.bodyToMono(SudoCodeRequest.class)
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body is required")))
                .flatMap(body -> sudoService.sendCode(body.method(), request.exchange()))
                .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> confirm(ServerRequest request) {
        return request.bodyToMono(SudoConfirmRequest.class)
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body is required")))
                .flatMap(body -> sudoService.confirm(body.method(), body.code(), request.exchange()))
                .then(ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.security.halo.run/v1alpha1");
    }

    /** Request body for sending a sudo verification code. */
    public record SudoCodeRequest(
            @Schema(requiredMode = REQUIRED) @NotBlank String method) {}

    /** Request body for confirming sudo. */
    public record SudoConfirmRequest(
            @Schema(requiredMode = REQUIRED) @NotBlank String method,
            @Schema(requiredMode = REQUIRED) @NotBlank String code) {}
}
