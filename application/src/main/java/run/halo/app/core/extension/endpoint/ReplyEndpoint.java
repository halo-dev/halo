package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.ListedReply;
import run.halo.app.content.comment.ReplyQuery;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;

/**
 * Endpoint for managing {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ReplyEndpoint implements CustomEndpoint {

    private final ReplyService replyService;

    public ReplyEndpoint(ReplyService replyService) {
        this.replyService = replyService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/Reply";
        return SpringdocRouteBuilder.route()
            .GET("replies", this::listReplies, builder -> {
                    builder.operationId("ListReplies")
                        .description("List replies.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedReply.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, ReplyQuery.class);
                }
            )
            .build();
    }

    Mono<ServerResponse> listReplies(ServerRequest request) {
        ReplyQuery replyQuery = new ReplyQuery(request.queryParams());
        return replyService.list(replyQuery)
            .flatMap(listedReplies -> ServerResponse.ok().bodyValue(listedReplies));
    }
}
