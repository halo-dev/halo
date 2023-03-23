package run.halo.app.extension.router;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

public class ExtensionRouterFunctionFactory {

    private final Scheme scheme;

    private final ReactiveExtensionClient client;

    public ExtensionRouterFunctionFactory(Scheme scheme, ReactiveExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @NonNull
    public RouterFunction<ServerResponse> create() {
        var getHandler = new ExtensionGetHandler(scheme, client);
        var listHandler = new ExtensionListHandler(scheme, client);
        var createHandler = new ExtensionCreateHandler(scheme, client);
        var updateHandler = new ExtensionUpdateHandler(scheme, client);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        // TODO More handlers here
        var gvk = scheme.groupVersionKind();
        var tagName = gvk.toString();
        return SpringdocRouteBuilder.route()
            .GET(getHandler.pathPattern(), getHandler,
                builder -> builder.operationId("Get" + gvk)
                    .description("Get " + gvk)
                    .tag(tagName)
                    .parameter(parameterBuilder().in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of " + scheme.singular()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response single " + scheme.singular())
                        .implementation(scheme.type())))
            .GET(listHandler.pathPattern(), listHandler,
                builder -> {
                    builder.operationId("List" + gvk)
                        .description("List " + gvk)
                        .tag(tagName)
                        .response(responseBuilder().responseCode("200")
                            .description("Response " + scheme.plural())
                            .implementation(ListResult.generateGenericClass(scheme)));
                    QueryParamBuildUtil.buildParametersFromType(builder, QueryListRequest.class);
                })
            .POST(createHandler.pathPattern(), createHandler,
                builder -> builder.operationId("Create" + gvk)
                    .description("Create " + gvk)
                    .tag(tagName)
                    .requestBody(requestBodyBuilder()
                        .description("Fresh " + scheme.singular())
                        .implementation(scheme.type()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response " + scheme.plural() + " created just now")
                        .implementation(scheme.type())))
            .PUT(updateHandler.pathPattern(), updateHandler,
                builder -> builder.operationId("Update" + gvk)
                    .description("Update " + gvk)
                    .tag(tagName)
                    .parameter(parameterBuilder().in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of " + scheme.singular()))
                    .requestBody(requestBodyBuilder()
                        .description("Updated " + scheme.singular())
                        .implementation(scheme.type()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response " + scheme.plural() + " updated just now")
                        .implementation(scheme.type())))
            .DELETE(deleteHandler.pathPattern(), deleteHandler,
                builder -> builder.operationId("Delete" + gvk)
                    .description("Delete " + gvk)
                    .tag(tagName)
                    .parameter(parameterBuilder().in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of " + scheme.singular()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response " + scheme.singular() + " deleted just now")))
            .build();
    }

    interface PathPatternGenerator {

        String pathPattern();

        static String buildExtensionPathPattern(Scheme scheme) {
            var gvk = scheme.groupVersionKind();
            StringBuilder pattern = new StringBuilder();
            if (gvk.hasGroup()) {
                pattern.append("/apis/").append(gvk.group());
            } else {
                pattern.append("/api");
            }
            return pattern.append('/').append(gvk.version()).append('/').append(scheme.plural())
                .toString();
        }
    }

    interface GetHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface ListHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface CreateHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface UpdateHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface DeleteHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

}
