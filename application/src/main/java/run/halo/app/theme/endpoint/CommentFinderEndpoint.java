package run.halo.app.theme.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.content.comment.CommentRequest;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.comment.ReplyRequest;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.CommentPublicQueryService;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * Endpoint for {@link CommentFinder}.
 */
@Component
@RequiredArgsConstructor
public class CommentFinderEndpoint implements CustomEndpoint {

    private final CommentPublicQueryService commentPublicQueryService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

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
                // fix gh-2951
                reply.getSpec().setHidden(false);
                return environmentFetcher.fetchComment()
                    .map(commentSetting -> {
                        if (isFalse(commentSetting.getEnable())) {
                            throw new AccessDeniedException(
                                "The comment function has been turned off.",
                                "problemDetail.comment.turnedOff", null);
                        }
                        if (checkReplyOwner(reply, commentSetting.getSystemUserOnly())) {
                            throw new AccessDeniedException("Allow only system users to comment.",
                                "problemDetail.comment.systemUsersOnly", null);
                        }
                        reply.getSpec()
                            .setApproved(isFalse(commentSetting.getRequireReviewForNew()));
                        return reply;
                    })
                    .defaultIfEmpty(reply);
            })
            .flatMap(reply -> replyService.create(commentName, reply))
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    private boolean checkReplyOwner(Reply reply, Boolean onlySystemUser) {
        Comment.CommentOwner owner = reply.getSpec().getOwner();
        if (isTrue(onlySystemUser)) {
            return owner != null && Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind());
        }
        return false;
    }

    Mono<ServerResponse> listComments(ServerRequest request) {
        CommentQuery commentQuery = new CommentQuery(request);
        var comparator = commentQuery.toComparator();
        return commentPublicQueryService.list(commentQuery.toRef(), commentQuery.getPage(),
                commentQuery.getSize(), comparator)
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    Mono<ServerResponse> getComment(ServerRequest request) {
        String name = request.pathVariable("name");
        return Mono.defer(() -> Mono.justOrEmpty(commentPublicQueryService.getByName(name)))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    Mono<ServerResponse> listCommentReplies(ServerRequest request) {
        String commentName = request.pathVariable("name");
        IListRequest.QueryListRequest queryParams =
            new IListRequest.QueryListRequest(request.queryParams());
        return commentPublicQueryService.listReply(commentName, queryParams.getPage(),
                queryParams.getSize())
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public static class CommentQuery extends PageableRequest {

        private final ServerWebExchange exchange;

        public CommentQuery(ServerRequest request) {
            super(request.queryParams());
            this.exchange = request.exchange();
        }

        @Schema(description = "The comment subject group.")
        public String getGroup() {
            return queryParams.getFirst("group");
        }

        @Schema(requiredMode = REQUIRED, description = "The comment subject version.")
        public String getVersion() {
            return emptyToNull(queryParams.getFirst("version"));
        }

        /**
         * Gets the {@link Ref}s kind.
         *
         * @return comment subject ref kind
         */
        @Schema(requiredMode = REQUIRED, description = "The comment subject kind.")
        public String getKind() {
            String kind = emptyToNull(queryParams.getFirst("kind"));
            if (kind == null) {
                throw new ServerWebInputException("The kind must not be null.");
            }
            return kind;
        }

        /**
         * Gets the {@link Ref}s name.
         *
         * @return comment subject ref name
         */
        @Schema(requiredMode = REQUIRED, description = "The comment subject name.")
        public String getName() {
            String name = emptyToNull(queryParams.getFirst("name"));
            if (name == null) {
                throw new ServerWebInputException("The name must not be null.");
            }
            return name;
        }

        @ArraySchema(uniqueItems = true,
            arraySchema = @Schema(name = "sort",
                description = "Sort property and direction of the list result. Supported fields: "
                    + "creationTimestamp"),
            schema = @Schema(description = "like field,asc or field,desc",
                implementation = String.class,
                example = "creationTimestamp,desc"))
        public Sort getSort() {
            return SortResolver.defaultInstance.resolve(exchange);
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

        public Comparator<Comment> toComparator() {
            var sort = getSort();
            var ctOrder = sort.getOrderFor("creationTimestamp");
            List<Comparator<Comment>> comparators = new ArrayList<>();
            if (ctOrder != null) {
                Comparator<Comment> comparator =
                    comparing(comment -> comment.getMetadata().getCreationTimestamp());
                if (ctOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparators.add(comparator);
                comparators.add(Comparators.compareName(true));
            }
            return comparators.stream()
                .reduce(Comparator::thenComparing)
                .orElse(null);
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
