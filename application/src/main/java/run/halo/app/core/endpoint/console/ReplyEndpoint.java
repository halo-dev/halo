package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentContentRequest;
import run.halo.app.content.comment.ListedReply;
import run.halo.app.content.comment.ReplyQuery;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ListResult;

/**
 * Console endpoint for listing replies under a comment.
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
        var tag = "ReplyV1alpha1Console";
        return SpringdocRouteBuilder.route()
                .GET("replies", this::listReplies, builder -> {
                    builder.operationId("ListReplies")
                            .description("List replies for a comment with pagination, sorting, labels, and field "
                                    + "selectors.")
                            .tag(tag)
                            .response(responseBuilder()
                                    .implementation(ListResult.generateGenericClass(ListedReply.class)));
                    ReplyQuery.buildParameters(builder);
                })
                .PUT(
                        "replies/{name}/content",
                        this::updateContent,
                        builder -> builder.operationId("UpdateReplyContent")
                                .description("Update only the body of an existing reply. Requires its current version.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .name("name")
                                        .in(ParameterIn.PATH)
                                        .required(true))
                                .requestBody(
                                        requestBodyBuilder().required(true).implementation(CommentContentRequest.class))
                                .response(responseBuilder().implementation(Reply.class)))
                .build();
    }

    Mono<ServerResponse> listReplies(ServerRequest request) {
        ReplyQuery replyQuery = new ReplyQuery(request.exchange());
        return replyService
                .list(replyQuery)
                .flatMap(listedReplies -> ServerResponse.ok().bodyValue(listedReplies));
    }

    Mono<ServerResponse> updateContent(ServerRequest request) {
        return request.bodyToMono(CommentContentRequest.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Request body required.")))
                .flatMap(body -> replyService.updateContent(request.pathVariable("name"), body))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated));
    }
}
