package run.halo.app.security.authentication.pat;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.security.PersonalAccessToken;

@Component
public class PatEndpoint implements CustomEndpoint {

    private final UserScopedPatHandler patHandler;

    public PatEndpoint(UserScopedPatHandler patHandler) {
        this.patHandler = patHandler;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = groupVersion().toString() + "/" + PersonalAccessToken.KIND;
        return route().nest(path("/personalaccesstokens"),
                () -> route()
                    .POST(patHandler::create,
                        builder -> builder
                            .tag(tag)
                            .operationId("GeneratePat")
                            .description("Generate a PAT.")
                            .requestBody(requestBodyBuilder()
                                .required(true)
                                .implementation(PersonalAccessToken.class))
                            .response(responseBuilder().implementation(PersonalAccessToken.class))
                    )
                    .GET(patHandler::list,
                        builder -> builder
                            .tag(tag)
                            .operationId("ObtainPats")
                            .description("Obtain PAT list.")
                            .response(responseBuilder()
                                .implementationArray(PersonalAccessToken.class)
                            )
                    )
                    .GET("/{name}", patHandler::get,
                        builder -> builder
                            .tag(tag)
                            .operationId("ObtainPat")
                            .description("Obtain a PAT.")
                            .parameter(parameterBuilder()
                                .in(ParameterIn.PATH)
                                .required(true)
                                .name("name")))
                    .PUT("/{name}/actions/revocation",
                        patHandler::revoke,
                        builder -> builder.tag(tag)
                            .operationId("RevokePat")
                            .description("Revoke a PAT")
                            .parameter(parameterBuilder()
                                .in(ParameterIn.PATH)
                                .required(true)
                                .name("name"))
                    )
                    .PUT("/{name}/actions/restoration",
                        patHandler::restore,
                        builder -> builder.tag(tag)
                            .operationId("RestorePat")
                            .description("Restore a PAT.")
                            .parameter(parameterBuilder()
                                .in(ParameterIn.PATH)
                                .required(true)
                                .name("name")
                            )
                    )
                    .DELETE("/{name}",
                        patHandler::delete,
                        builder -> builder.tag(tag)
                            .operationId("DeletePat")
                            .description("Delete a PAT")
                            .parameter(parameterBuilder()
                                .in(ParameterIn.PATH)
                                .required(true)
                                .name("name")
                            ))
                    .build(),
                builder -> builder.description("User-scoped PersonalAccessToken endpoint"))
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.security.halo.run/v1alpha1");
    }

}
