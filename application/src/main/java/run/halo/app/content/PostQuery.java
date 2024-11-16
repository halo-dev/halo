package run.halo.app.content;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.core.extension.content.Post.PUBLISHED_LABEL;
import static run.halo.app.core.extension.content.Post.PostPhase.PENDING_APPROVAL;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostQuery extends SortableRequest {

    private final String username;

    public PostQuery(ServerRequest request) {
        this(request, null);
    }

    public PostQuery(ServerRequest request, @Nullable String username) {
        super(request.exchange());
        this.username = username;
    }

    @Nullable
    public String getPublishPhase() {
        return queryParams.getFirst("publishPhase");
    }

    @Nullable
    public String getCategoryWithChildren() {
        var value = queryParams.getFirst("categoryWithChildren");
        return StringUtils.defaultIfBlank(value, null);
    }

    @Nullable
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    /**
     * Build a list options from the query object.
     *
     * @return a list options
     */
    @Override
    public ListOptions toListOptions() {
        var builder = ListOptions.builder(super.toListOptions());

        Optional.ofNullable(getKeyword())
            .filter(StringUtils::isNotBlank)
            .ifPresent(keyword -> builder.andQuery(or(
                contains("status.excerpt", keyword),
                contains("spec.slug", keyword),
                contains("spec.title", keyword)
            )));

        Optional.ofNullable(getPublishPhase())
            .filter(StringUtils::isNotBlank)
            .map(Post.PostPhase::from)
            .ifPresent(phase -> {
                if (PENDING_APPROVAL.equals(phase)) {
                    builder.andQuery(equal("status.phase", phase.name()));
                }
                var labelSelector = builder.labelSelector();
                Optional.of(phase)
                    .filter(Post.PostPhase.PUBLISHED::equals)
                    .ifPresentOrElse(
                        published -> labelSelector.eq(PUBLISHED_LABEL, Boolean.TRUE.toString()),
                        () -> labelSelector.notEq(PUBLISHED_LABEL, Boolean.TRUE.toString())
                    );
            });

        Optional.ofNullable(username)
            .filter(StringUtils::isNotBlank)
            .ifPresent(username -> builder.andQuery(equal("spec.owner", username)));

        return builder.build();
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("publishPhase")
                .description("Posts filtered by publish phase.")
                .implementation(Post.PostPhase.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Posts filtered by keyword.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("categoryWithChildren")
                .description("Posts filtered by category including sub-categories.")
                .implementation(String.class)
                .required(false));
    }
}
