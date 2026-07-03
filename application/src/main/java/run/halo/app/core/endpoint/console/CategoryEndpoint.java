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
public class CategoryEndpoint implements CustomEndpoint {

    private final CategoryConsoleService categoryConsoleService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "CategoryV1alpha1Console";
        return SpringdocRouteBuilder.route()
                .GET(
                        "categories/-/tree",
                        this::listTree,
                        builder -> builder.operationId("ListCategoryTree")
                                .description("List Categories as a canonical tree for console category management.")
                                .tag(tag)
                                .response(responseBuilder().implementationArray(CategoryTreeNode.class)))
                .PUT(
                        "categories/{name}/position",
                        this::updatePosition,
                        builder -> builder.operationId("UpdateCategoryPosition")
                                .description("Move a Category within the Console category tree.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .in(ParameterIn.PATH)
                                        .name("name")
                                        .required(true)
                                        .description("Category metadata.name"))
                                .requestBody(requestBodyBuilder().implementation(CategoryPositionRequest.class))
                                .response(responseBuilder().implementationArray(CategoryTreeNode.class)))
                .build();
    }

    private Mono<ServerResponse> listTree(ServerRequest request) {
        return categoryConsoleService
                .listTree()
                .flatMap(tree -> ServerResponse.ok().bodyValue(tree));
    }

    private Mono<ServerResponse> updatePosition(ServerRequest request) {
        var name = request.pathVariable("name");
        return request.bodyToMono(CategoryPositionRequest.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Request body is required.")))
                .flatMap(positionRequest -> categoryConsoleService.updatePosition(name, positionRequest))
                .flatMap(tree -> ServerResponse.ok().bodyValue(tree));
    }
}
