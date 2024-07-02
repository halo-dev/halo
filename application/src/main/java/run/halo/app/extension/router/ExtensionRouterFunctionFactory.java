package run.halo.app.extension.router;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;

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
        var patchHandler = new ExtensionPatchHandler(scheme, client);
        // TODO More handlers here
        var gvk = scheme.groupVersionKind();
        var kind = gvk.kind();
        var tagName = gvk.kind() + StringUtils.capitalize(gvk.version());
        return SpringdocRouteBuilder.route()
            .GET(getHandler.pathPattern(), getHandler,
                builder -> builder.operationId("get" + kind)
                    .description("Get " + kind)
                    .tag(tagName)
                    .parameter(parameterBuilder().in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of " + scheme.singular()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response single " + scheme.singular())
                        .implementation(scheme.type())))
            .GET(listHandler.pathPattern(), listHandler,
                builder -> {
                    builder.operationId("list" + kind)
                        .description("List " + kind)
                        .tag(tagName)
                        .response(responseBuilder().responseCode("200")
                            .description("Response " + scheme.plural())
                            .implementation(ListResult.generateGenericClass(scheme)));
                    SortableRequest.buildParameters(builder);
                })
            .POST(createHandler.pathPattern(), createHandler,
                builder -> builder.operationId("create" + kind)
                    .description("Create " + kind)
                    .tag(tagName)
                    .requestBody(requestBodyBuilder()
                        .description("Fresh " + scheme.singular())
                        .implementation(scheme.type()))
                    .response(responseBuilder().responseCode("200")
                        .description("Response " + scheme.plural() + " created just now")
                        .implementation(scheme.type())))
            .PUT(updateHandler.pathPattern(), updateHandler,
                builder -> builder.operationId("update" + kind)
                    .description("Update " + kind)
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
            .PATCH(patchHandler.pathPattern(), patchHandler,
                builder -> builder.operationId("patch" + kind)
                    .description("Patch " + kind)
                    .tag(tagName)
                    .parameter(parameterBuilder().in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of " + scheme.singular()))
                    .requestBody(requestBodyBuilder()
                        .content(contentBuilder()
                            .mediaType("application/json-patch+json")
                            .schema(
                                schemaBuilder().ref(RefUtils.constructRef(JsonPatch.SCHEMA_NAME))
                            )
                        )
                    )
                    .response(responseBuilder().responseCode("200")
                        .description("Response " + scheme.singular() + " patched just now")
                        .implementation(scheme.type())
                    )
            )
            .DELETE(deleteHandler.pathPattern(), deleteHandler,
                builder -> builder.operationId("delete" + kind)
                    .description("Delete " + kind)
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

    interface PatchHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

}
