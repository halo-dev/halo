package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.content.comment.CommentRequest;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.comment.ReplyRequest;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * Endpoint for {@link CommentFinder}.
 */
@Component
public class CommentFinderEndpoint implements CustomEndpoint {

    private final CommentFinder commentFinder;
    private final CommentService commentService;
    private final ReplyService replyService;

    /**
     * Construct a {@link CommentFinderEndpoint} instance.
     *
     * @param commentFinder comment finder
     * @param commentService comment service to create comment
     * @param replyService reply service to create reply
     */
    public CommentFinderEndpoint(CommentFinder commentFinder, CommentService commentService,
        ReplyService replyService) {
        this.commentFinder = commentFinder;
        this.commentService = commentService;
        this.replyService = replyService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/Comment";
        return SpringdocRouteBuilder.route()
            .POST("comments", this::createComment,
                builder -> builder.operationId("CreateComment")
                    .description("Create a comment.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(CommentRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Comment.class))
            )
            .POST("comments/{name}/reply", this::createReply,
                builder -> builder.operationId("CreateReply")
                    .description("Create a reply.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(ReplyRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Reply.class))
            )
            .GET("comments", this::listComments, builder -> {
                builder.operationId("ListComments")
                    .description("List comments.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ListResult.generateGenericClass(CommentVo.class))
                    );
                QueryParamBuildUtil.buildParametersFromType(builder, CommentQuery.class);
            })
            .GET("comments/{name}", this::getComment, builder -> {
                builder.operationId("GetComment")
                    .description("Get a comment.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementation(ListResult.generateGenericClass(CommentVo.class))
                    );
            })
            .GET("comments/{name}/reply", this::listCommentReplies, builder -> {
                builder.operationId("ListCommentReplies")
                    .description("List comment replies.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementation(ListResult.generateGenericClass(ReplyVo.class))
                    );
                QueryParamBuildUtil.buildParametersFromType(builder, PageableRequest.class);
            })
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.halo.run/v1alpha1");
    }

    Mono<ServerResponse> createComment(ServerRequest request) {
        return request.bodyToMono(CommentRequest.class)
            .flatMap(commentRequest -> {
                Comment comment = commentRequest.toComment();
                comment.getSpec().setIpAddress(IpAddressUtils.getIpAddress(request));
                comment.getSpec().setUserAgent(HaloUtils.userAgentFrom(request));
                return commentService.create(comment);
            })
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    Mono<ServerResponse> createReply(ServerRequest request) {
        String commentName = request.pathVariable("name");
        return request.bodyToMono(ReplyRequest.class)
            .flatMap(replyRequest -> {
                Reply reply = replyRequest.toReply();
                reply.getSpec().setIpAddress(IpAddressUtils.getIpAddress(request));
                reply.getSpec().setUserAgent(HaloUtils.userAgentFrom(request));
                return replyService.create(commentName, reply);
            })
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    Mono<ServerResponse> listComments(ServerRequest request) {
        CommentQuery commentQuery = new CommentQuery(request.queryParams());
        return commentFinder.list(commentQuery.toRef(), commentQuery.getPage(),
                    commentQuery.getSize())
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    Mono<ServerResponse> getComment(ServerRequest request) {
        String name = request.pathVariable("name");
        return Mono.defer(() -> Mono.justOrEmpty(commentFinder.getByName(name)))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    Mono<ServerResponse> listCommentReplies(ServerRequest request) {
        String commentName = request.pathVariable("name");
        IListRequest.QueryListRequest queryParams =
            new IListRequest.QueryListRequest(request.queryParams());
        return commentFinder.listReply(commentName, queryParams.getPage(), queryParams.getSize())
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public static class CommentQuery extends PageableRequest {

        public CommentQuery(MultiValueMap<String, String> queryParams) {
            super(queryParams);
        }

        @Schema(description = "The comment subject group.")
        public String getGroup() {
            return queryParams.getFirst("group");
        }

        @Schema(required = true, description = "The comment subject version.")
        public String getVersion() {
            return emptyToNull(queryParams.getFirst("version"));
        }

        /**
         * Gets the {@link Ref}s kind.
         *
         * @return comment subject ref kind
         */
        @Schema(required = true, description = "The comment subject kind.")
        public String getKind() {
            String kind = emptyToNull(queryParams.getFirst("kind"));
            if (kind == null) {
                throw new IllegalArgumentException("The kind must not be null.");
            }
            return kind;
        }

        /**
         * Gets the {@link Ref}s name.
         *
         * @return comment subject ref name
         */
        @Schema(required = true, description = "The comment subject name.")
        public String getName() {
            String name = emptyToNull(queryParams.getFirst("name"));
            if (name == null) {
                throw new IllegalArgumentException("The name must not be null.");
            }
            return name;
        }

        Ref toRef() {
            Ref ref = new Ref();
            ref.setGroup(getGroup());
            ref.setKind(getKind());
            ref.setVersion(getVersion());
            ref.setName(getName());
            return ref;
        }

        String emptyToNull(String str) {
            return StringUtils.isBlank(str) ? null : str;
        }
    }

    public static class PageableRequest extends IListRequest.QueryListRequest {

        public PageableRequest(MultiValueMap<String, String> queryParams) {
            super(queryParams);
        }

        @Override
        @JsonIgnore
        public List<String> getLabelSelector() {
            throw new UnsupportedOperationException("Unsupported this parameter");
        }

        @Override
        @JsonIgnore
        public List<String> getFieldSelector() {
            throw new UnsupportedOperationException("Unsupported this parameter");
        }
    }
}
