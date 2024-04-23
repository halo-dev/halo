package run.halo.app.search;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;

public class SearchParam {

    private static final int DEFAULT_LIMIT = 10;
    private static final String DEFAULT_HIGHLIGHT_PRE_TAG = "<B>";
    private static final String DEFAULT_HIGHLIGHT_POST_TAG = "</B>";

    private final MultiValueMap<String, String> query;

    public SearchParam(MultiValueMap<String, String> query) {
        this.query = query;
    }

    @Schema(name = "keyword", requiredMode = REQUIRED)
    public String getKeyword() {
        var keyword = query.getFirst("keyword");
        if (!StringUtils.hasText(keyword)) {
            throw new ServerWebInputException("keyword is required");
        }
        return keyword;
    }

    @Schema(name = "limit", defaultValue = "100", maximum = "1000")
    public int getLimit() {
        var limitString = query.getFirst("limit");
        int limit = 0;
        if (StringUtils.hasText(limitString)) {
            try {
                limit = Integer.parseInt(limitString);
            } catch (NumberFormatException nfe) {
                throw new ServerWebInputException("Failed to get ");
            }
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        return limit;
    }

    @Schema(name = "highlightPreTag", defaultValue = DEFAULT_HIGHLIGHT_PRE_TAG)
    public String getHighlightPreTag() {
        var highlightPreTag = query.getFirst("highlightPreTag");
        if (!StringUtils.hasText(highlightPreTag)) {
            highlightPreTag = DEFAULT_HIGHLIGHT_PRE_TAG;
        }
        return highlightPreTag;
    }

    @Schema(name = "highlightPostTag", defaultValue = DEFAULT_HIGHLIGHT_POST_TAG)
    public String getHighlightPostTag() {
        var highlightPostTag = query.getFirst("highlightPostTag");
        if (!StringUtils.hasText(highlightPostTag)) {
            highlightPostTag = DEFAULT_HIGHLIGHT_POST_TAG;
        }
        return highlightPostTag;
    }

    public static void buildParameters(Builder builder) {
        builder.parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Keyword to search")
                .implementation(String.class)
                .required(true))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("limit")
                .description("Limit of search results")
                .required(false)
                .schema(schemaBuilder()
                    .implementation(Integer.class)
                    .maximum("1000")
                    .defaultValue(String.valueOf(DEFAULT_LIMIT))))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("highlightPreTag")
                .description("Highlight pre tag")
                .required(false)
                .schema(schemaBuilder()
                    .implementation(String.class)
                    .defaultValue(DEFAULT_HIGHLIGHT_PRE_TAG)
                ))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("highlightPostTag")
                .description("Highlight post tag")
                .required(false)
                .schema(schemaBuilder()
                    .implementation(String.class)
                    .defaultValue(DEFAULT_HIGHLIGHT_POST_TAG)
                )
            );

    }
}
