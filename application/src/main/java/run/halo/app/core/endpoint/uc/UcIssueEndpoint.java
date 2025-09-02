package run.halo.app.core.endpoint.uc;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.IssueQuery;
import run.halo.app.content.IssueService;
import run.halo.app.content.ListedIssue;
import run.halo.app.core.extension.content.Issue;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.exception.NotFoundException;

/**
 * User center endpoint for issues.
 *
 * @author halo-copilot
 * @since 2.21.0
 */
@Component
public class UcIssueEndpoint implements CustomEndpoint {

    private final IssueService issueService;

    public UcIssueEndpoint(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "IssueV1alpha1Uc";
        var namePathParam = parameterBuilder().name("name")
            .description("Issue name")
            .in(ParameterIn.PATH)
            .required(true);
        
        return route().nest(
                path("/issues"),
                () -> route()
                    .GET(this::listMyIssues, builder -> {
                            builder.operationId("ListMyIssues")
                                .description("List issues owned by the current user.")
                                .tag(tag)
                                .response(responseBuilder().implementation(
                                    ListResult.generateGenericClass(ListedIssue.class)));
                            buildParameters(builder);
                        }
                    )
                    .POST(this::createMyIssue, builder -> builder.operationId("CreateMyIssue")
                        .tag(tag)
                        .description("Create my issue.")
                        .requestBody(requestBodyBuilder().implementation(Issue.class))
                        .response(responseBuilder().implementation(Issue.class))
                    )
                    .GET("/{name}", this::getMyIssue, builder -> builder.operationId("GetMyIssue")
                        .tag(tag)
                        .parameter(namePathParam)
                        .description("Get issue that belongs to the current user.")
                        .response(responseBuilder().implementation(Issue.class))
                    )
                    .PUT("/{name}", this::updateMyIssue, builder ->
                        builder.operationId("UpdateMyIssue")
                            .tag(tag)
                            .parameter(namePathParam)
                            .description("Update my issue.")
                            .requestBody(requestBodyBuilder().implementation(Issue.class))
                            .response(responseBuilder().implementation(Issue.class))
                    )
                    .DELETE("/{name}", this::deleteMyIssue, builder -> builder.tag(tag)
                        .operationId("DeleteMyIssue")
                        .description("Delete my issue.")
                        .parameter(namePathParam)
                        .response(responseBuilder().implementation(Issue.class))
                    )
                    .build(),
                builder -> {
                })
            .build();
    }

    private Mono<ServerResponse> listMyIssues(ServerRequest request) {
        return getCurrentUser()
            .map(username -> new IssueQuery(request, username))
            .flatMap(issueService::listIssues)
            .flatMap(issues -> ServerResponse.ok().bodyValue(issues));
    }

    private Mono<ServerResponse> createMyIssue(ServerRequest request) {
        return getCurrentUser()
            .flatMap(username -> request.bodyToMono(Issue.class)
                .doOnNext(issue -> issue.getSpec().setOwner(username))
                .flatMap(issueService::createIssue)
            )
            .flatMap(issue -> ServerResponse.ok().bodyValue(issue));
    }

    private Mono<ServerResponse> getMyIssue(ServerRequest request) {
        var issueName = request.pathVariable("name");
        return getCurrentUser()
            .flatMap(username -> issueService.getByUsername(issueName, username))
            .flatMap(issue -> ServerResponse.ok().bodyValue(issue));
    }

    private Mono<ServerResponse> updateMyIssue(ServerRequest request) {
        var issueName = request.pathVariable("name");
        return getCurrentUser()
            .flatMap(username -> issueService.getByUsername(issueName, username)
                .flatMap(existing -> request.bodyToMono(Issue.class)
                    .doOnNext(updated -> {
                        updated.getMetadata().setName(existing.getMetadata().getName());
                        updated.getMetadata().setVersion(existing.getMetadata().getVersion());
                        updated.getSpec().setOwner(username);
                    })
                    .flatMap(issueService::updateIssue)
                )
            )
            .flatMap(issue -> ServerResponse.ok().bodyValue(issue));
    }

    private Mono<ServerResponse> deleteMyIssue(ServerRequest request) {
        var issueName = request.pathVariable("name");
        return getCurrentUser()
            .flatMap(username -> issueService.deleteByUsername(issueName, username))
            .flatMap(issue -> ServerResponse.ok().bodyValue(issue));
    }

    private Mono<String> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.content.halo.run/v1alpha1");
    }

    private void buildParameters(org.springdoc.core.fn.builders.operation.Builder builder) {
        builder.parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(Integer.class)
                .description("The page number. Default is 0.")
            )
            .parameter(parameterBuilder()
                .name("size")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(Integer.class)
                .description("Size of each page. Default is 20.")
            )
            .parameter(parameterBuilder()
                .name("status")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(String.class)
                .description("Filter by status. e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED")
            )
            .parameter(parameterBuilder()
                .name("priority")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(String.class)
                .description("Filter by priority. e.g. LOW, MEDIUM, HIGH, CRITICAL")
            )
            .parameter(parameterBuilder()
                .name("type")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(String.class)
                .description("Filter by type. e.g. BUG, FEATURE_REQUEST, IMPROVEMENT, QUESTION")
            )
            .parameter(parameterBuilder()
                .name("keyword")
                .in(ParameterIn.QUERY)
                .required(false)
                .implementation(String.class)
                .description("Filter by keyword in title or description")
            );
    }
}