package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentQuery;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.comment.ListedComment;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;


/**
 * Endpoint for managing comment.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CommentEndpoint implements CustomEndpoint {

    private final CommentService commentService;

    public CommentEndpoint(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/Comment";
        return SpringdocRouteBuilder.route()
            .GET("comments", this::listComments, builder -> {
                    builder.operationId("ListComments")
                        .description("List comments.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedComment.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, CommentQuery.class);
                }
            )
            .build();
    }

    Mono<ServerResponse> listComments(ServerRequest request) {
        CommentQuery commentQuery = new CommentQuery(request.queryParams());
        return commentService.listComment(commentQuery)
            .flatMap(listedComments -> ServerResponse.ok().bodyValue(listedComments));
    }

}
