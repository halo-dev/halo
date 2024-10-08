package run.halo.app.core.attachment;

import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.not;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import java.util.Optional;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.extension.router.SortableRequest;

public class SearchRequest extends SortableRequest {

    public SearchRequest(ServerRequest request) {
        super(request.exchange());
    }

    public Optional<String> getKeyword() {
        return Optional.ofNullable(queryParams.getFirst("keyword"))
            .filter(StringUtils::hasText);
    }

    public Optional<Boolean> getUngrouped() {
        return Optional.ofNullable(queryParams.getFirst("ungrouped"))
            .map(ungroupedStr -> getSharedInstance().convert(ungroupedStr, Boolean.class));
    }

    public Optional<List<String>> getAccepts() {
        return Optional.ofNullable(queryParams.get("accepts"))
            .filter(accepts -> !accepts.isEmpty()
                && !accepts.contains("*")
                && !accepts.contains("*/*")
            );
    }

    public ListOptions toListOptions(List<String> hiddenGroups) {
        var builder = ListOptions.builder(super.toListOptions());

        getKeyword().ifPresent(keyword -> {
            builder.andQuery(contains("spec.displayName", keyword));
        });

        getUngrouped()
            .filter(ungrouped -> ungrouped)
            .ifPresent(ungrouped -> builder.andQuery(isNull("spec.groupName")));

        if (!CollectionUtils.isEmpty(hiddenGroups)) {
            builder.andQuery(not(in("spec.groupName", hiddenGroups)));
        }

        getAccepts().flatMap(accepts -> accepts.stream()
                .filter(StringUtils::hasText)
                .map(accept -> accept.replace("/*", "/").toLowerCase())
                .distinct()
                .map(accept -> startsWith("spec.mediaType", accept))
                .reduce(QueryFactory::or)
            )
            .ifPresent(builder::andQuery);

        return builder.build();
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(QueryParamBuildUtil.sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("ungrouped")
                .required(false)
                .description("""
                    Filter attachments without group. This parameter will ignore group \
                    parameter.\
                    """)
                .implementation(Boolean.class))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .required(false)
                .description("Keyword for searching.")
                .implementation(String.class))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("accepts")
                .required(false)
                .description("Acceptable media types.")
                .array(
                    arraySchemaBuilder()
                        .uniqueItems(true)
                        .schema(schemaBuilder()
                            .implementation(String.class)
                            .example("image/*"))
                )
                .implementationArray(String.class)
            );
    }
}
