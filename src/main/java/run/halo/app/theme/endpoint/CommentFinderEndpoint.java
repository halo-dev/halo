package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * Endpoint for {@link CommentFinder}.
 */
@Component
public class CommentFinderEndpoint implements CustomEndpoint {

    private final CommentFinder commentFinder;

    public CommentFinderEndpoint(CommentFinder commentFinder) {
        this.commentFinder = commentFinder;
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

    Mono<ServerResponse> listComments(ServerRequest request) {
        CommentQuery commentQuery = new CommentQuery(request.queryParams());
        return Mono.defer(() -> Mono.just(
                commentFinder.list(commentQuery.toRef(), commentQuery.getPage(),
                    commentQuery.getSize())))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    Mono<ServerResponse> getComment(ServerRequest request) {
        String name = request.pathVariable("name");
        return Mono.defer(() -> Mono.justOrEmpty(commentFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(comment -> ServerResponse.ok().bodyValue(comment));
    }

    Mono<ServerResponse> listCommentReplies(ServerRequest request) {
        String commentName = request.pathVariable("name");
        IListRequest.QueryListRequest queryParams =
            new IListRequest.QueryListRequest(request.queryParams());
        return Mono.defer(() -> Mono.just(
                commentFinder.listReply(commentName, queryParams.getPage(), queryParams.getSize())))
            .publishOn(Schedulers.boundedElastic())
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
