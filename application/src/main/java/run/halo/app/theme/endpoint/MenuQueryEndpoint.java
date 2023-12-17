package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.finders.MenuFinder;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * Endpoint for menu query APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class MenuQueryEndpoint implements CustomEndpoint {

    private final MenuFinder menuFinder;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Menu";
        return SpringdocRouteBuilder.route()
            .GET("menus/-", this::getByName,
                builder -> builder.operationId("queryPrimaryMenu")
                    .description("Gets primary menu.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(MenuVo.class)
                    )
            )
            .GET("menus/{name}", this::getByName,
                builder -> builder.operationId("queryMenuByName")
                    .description("Gets menu by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Menu name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(MenuVo.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> getByName(ServerRequest request) {
        return determineMenuName(request)
            .flatMap(menuFinder::getByName)
            .flatMap(menuVo -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(menuVo)
            );
    }

    private Mono<String> determineMenuName(ServerRequest request) {
        String name = request.pathVariables().getOrDefault("name", "-");
        if (!"-".equals(name)) {
            return Mono.just(name);
        }
        // If name is "-", then get primary menu.
        return environmentFetcher.fetch(SystemSetting.Menu.GROUP, SystemSetting.Menu.class)
            .mapNotNull(SystemSetting.Menu::getPrimary)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Primary menu is not configured."))
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Menu());
    }
}
