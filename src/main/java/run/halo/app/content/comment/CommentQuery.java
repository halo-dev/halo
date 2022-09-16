package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.router.IListRequest;

/**
 * Query criteria for comment list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentQuery extends IListRequest.QueryListRequest {

    public CommentQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
    }

    @Schema(description = "Comments filtered by keyword.")
    public String getKeyword() {
        String keyword = queryParams.getFirst("keyword");
        return StringUtils.isBlank(keyword) ? null : keyword;
    }

    @Schema(description = "Comments approved.")
    public Boolean getApproved() {
        return convertBooleanOrNull(queryParams.getFirst("approved"));
    }

    @Schema(description = "The comment is hidden from the theme side.")
    public Boolean getHidden() {
        return convertBooleanOrNull(queryParams.getFirst("hidden"));
    }

    @Schema(description = "Send notifications when there are new replies.")
    public Boolean getAllowNotification() {
        return convertBooleanOrNull(queryParams.getFirst("allowNotification"));
    }

    @Schema(description = "Comment top display.")
    public Boolean getTop() {
        return convertBooleanOrNull(queryParams.getFirst("top"));
    }

    @Schema(description = "Commenter kind.")
    public String getOwnerKind() {
        String ownerKind = queryParams.getFirst("ownerKind");
        return StringUtils.isBlank(ownerKind) ? null : ownerKind;
    }

    @Schema(description = "Commenter name.")
    public String getOwnerName() {
        String ownerName = queryParams.getFirst("ownerName");
        return StringUtils.isBlank(ownerName) ? null : ownerName;
    }

    @Schema(description = "Comment subject kind.")
    public String getSubjectKind() {
        String subjectKind = queryParams.getFirst("subjectKind");
        return StringUtils.isBlank(subjectKind) ? null : subjectKind;
    }

    @Schema(description = "Comment subject name.")
    public String getSubjectName() {
        String subjectName = queryParams.getFirst("subjectName");
        return StringUtils.isBlank(subjectName) ? null : subjectName;
    }

    @Schema(description = "Comment collation.")
    public CommentSorter getSort() {
        String sort = queryParams.getFirst("sort");
        return CommentSorter.convertFrom(sort);
    }

    @Schema(description = "ascending order If it is true; otherwise, it is in descending order.")
    public Boolean getSortOrder() {
        String sortOrder = queryParams.getFirst("sortOrder");
        return convertBooleanOrNull(sortOrder);
    }

    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
