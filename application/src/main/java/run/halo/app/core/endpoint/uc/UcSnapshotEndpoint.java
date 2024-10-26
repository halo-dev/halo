package run.halo.app.core.endpoint.uc;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
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
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.content.SnapshotService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Ref;
import run.halo.app.infra.exception.NotFoundException;

@Component
public class UcSnapshotEndpoint implements CustomEndpoint {

    private final PostService postService;

    private final SnapshotService snapshotService;

    public UcSnapshotEndpoint(PostService postService, SnapshotService snapshotService) {
        this.postService = postService;
        this.snapshotService = snapshotService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "SnapshotV1alpha1Uc";
        return route().nest(path("/snapshots"),
                () -> route()
                    .GET("/{name}",
                        this::getSnapshot,
                        builder -> builder.operationId("GetSnapshotForPost")
                            .description("Get snapshot for one post.")
                            .parameter(parameterBuilder()
                                .name("name")
                                .in(ParameterIn.PATH)
                                .required(true)
                                .description("Snapshot name.")
                            )
                            .parameter(parameterBuilder()
                                .name("postName")
                                .in(ParameterIn.QUERY)
                                .required(true)
                                .description("Post name.")
                            )
                            .parameter(parameterBuilder()
                                .name("patched")
                                .in(ParameterIn.QUERY)
                                .required(false)
                                .implementation(Boolean.class)
                                .description("Should include patched content and raw or not.")
                            )
                            .response(responseBuilder().implementation(Snapshot.class))
                            .tag(tag))
                    .build(),
                builder -> {
                })
            .build();
    }

    private Mono<ServerResponse> getSnapshot(ServerRequest request) {
        var snapshotName = request.pathVariable("name");
        var postName = request.queryParam("postName")
            .orElseThrow(() -> new ServerWebInputException("Query parameter postName is required"));
        var patched = request.queryParam("patched").map(Boolean::valueOf).orElse(false);

        var postNotFoundError = Mono.<Post>error(
            () -> new NotFoundException("The post was not found or deleted.")
        );
        var snapshotNotFoundError = Mono.<Snapshot>error(
            () -> new NotFoundException("The snapshot was not found or deleted.")
        );

        var postMono = getCurrentUser().flatMap(username ->
            postService.getByUsername(postName, username).switchIfEmpty(postNotFoundError)
        );

        // check the post belongs to the current user.
        var snapshotMono = postMono.flatMap(post -> Mono.defer(
                () -> {
                    if (patched) {
                        var baseSnapshotName = post.getSpec().getBaseSnapshot();
                        return snapshotService.getPatchedBy(snapshotName, baseSnapshotName);
                    }
                    return snapshotService.getBy(snapshotName);
                })
            .filter(snapshot -> {
                var subjectRef = snapshot.getSpec().getSubjectRef();
                return Ref.equals(subjectRef, post);
            })
            .switchIfEmpty(snapshotNotFoundError)
        );

        return ServerResponse.ok().body(snapshotMono, Snapshot.class);
    }

    private Mono<String> getCurrentUser() {
        return ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.content.halo.run/v1alpha1");
    }

}
