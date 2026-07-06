package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;

@Component
@RequiredArgsConstructor
public class MenuItemEndpoint implements CustomEndpoint {

    private final MenuItemConsoleService menuItemConsoleService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "MenuItemV1alpha1Console";
        return SpringdocRouteBuilder.route()
                .GET(
                        "menuitems/-/tree",
                        this::listTree,
                        builder -> builder.operationId("ListMenuItemTree")
                                .description("List MenuItems as a canonical tree for console menu management.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .in(ParameterIn.QUERY)
                                        .name("menuName")
                                        .required(true)
                                        .description("Menu metadata.name"))
                                .response(responseBuilder().implementationArray(MenuItemTreeNode.class)))
                .PUT(
                        "menuitems/{name}/position",
                        this::updatePosition,
                        builder -> builder.operationId("UpdateMenuItemPosition")
                                .description("Move a MenuItem within its owning Menu.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .in(ParameterIn.PATH)
                                        .name("name")
                                        .required(true)
                                        .description("MenuItem metadata.name"))
                                .requestBody(requestBodyBuilder().implementation(MenuItemPositionRequest.class))
                                .response(responseBuilder().implementationArray(MenuItemTreeNode.class)))
                .build();
    }

    private Mono<ServerResponse> listTree(ServerRequest request) {
        var menuName = request.queryParam("menuName")
                .filter(org.springframework.util.StringUtils::hasText)
                .orElseThrow(() -> new ServerWebInputException("Query parameter menuName is required."));
        return menuItemConsoleService
                .listTree(menuName)
                .flatMap(tree -> ServerResponse.ok().bodyValue(tree));
    }

    private Mono<ServerResponse> updatePosition(ServerRequest request) {
        var name = request.pathVariable("name");
        return request.bodyToMono(MenuItemPositionRequest.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Request body is required.")))
                .flatMap(positionRequest -> menuItemConsoleService.updatePosition(name, positionRequest))
                .flatMap(tree -> ServerResponse.ok().bodyValue(tree));
    }
}
