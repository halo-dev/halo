package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.router.IListRequest;

/**
 * Query criteria for {@link Reply} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReplyQuery extends IListRequest.QueryListRequest {

    public ReplyQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
    }

    @Schema(description = "Replies filtered by commentName.")
    public String getCommentName() {
        String commentName = queryParams.getFirst("commentName");
        return StringUtils.isBlank(commentName) ? null : commentName;
    }
}
