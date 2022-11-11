package run.halo.app.search;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "keyword", required = true)
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
}
