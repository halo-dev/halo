package run.halo.app.handler.migrate.support.wordpress;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * <p>  </p>
 *
 * @author guqing
 * @date 2019-11-17 14:01
 */
@Data
public class Comment {
    @JSONField(name = "wp:comment_id")
    private String commentId;

    @JSONField(name = "wp:comment_author")

    private String commentAuthor;

    @JSONField(name = "wp:comment_author_email")
    private String commentAuthorEmail;

    @JSONField(name = "wp:comment_author_url")
    private String commentAuthorUrl;

    @JSONField(name = "wp:comment_author_IP")
    private String commentAuthorIp;

    @JSONField(name = "wp:comment_date")
    private String commentDate;

    @JSONField(name = "wp:comment_content")
    private String commentContent;

    @JSONField(name = "wp:comment_approved")
    private String commentApproved;

    @JSONField(name = "wp:comment_parent")
    private Long commentParent;

    @JSONField(name = "wp:comment_user_id")
    private String commentUserId;
}
