package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.endpoint.CustomEndpoint;

@Component
@RequiredArgsConstructor
public class MenuEndpoint implements CustomEndpoint {

    private final MenuConsoleService menuConsoleService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "MenuV1alpha1Console";
        return SpringdocRouteBuilder.route()
                .DELETE(
                        "menus/{name}",
                        this::deleteMenu,
                        builder -> builder.operationId("DeleteMenu")
                                .description("Delete a Menu and MenuItems owned by the Menu.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .in(ParameterIn.PATH)
                                        .name("name")
                                        .required(true)
                                        .description("Menu metadata.name"))
                                .response(responseBuilder().implementation(Menu.class)))
                .build();
    }

    private Mono<ServerResponse> deleteMenu(ServerRequest request) {
        var name = request.pathVariable("name");
        return menuConsoleService
                .deleteMenu(name)
                .flatMap(menu -> ServerResponse.ok().bodyValue(menu));
    }
}
