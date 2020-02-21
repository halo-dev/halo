package run.halo.app.handler.migrate.support.wordpress;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import run.halo.app.handler.migrate.utils.PropertyMappingTo;

/**
 * <p>
 * WordPress导出的xml中对于的comment节点下的子节点值将被映射为该类的属性,
 * 最终会被转换为{@link run.halo.app.model.entity.PostComment}
 * </p>
 *
 * @author guqing
 * @author ryanwang
 * @date 2019-11-17 14:01
 */
@Data
public class Comment {

    @JSONField(name = "wp:comment_id")
    @PropertyMappingTo("id")
    private String commentId;

    @JSONField(name = "wp:comment_author")
    @PropertyMappingTo("author")
    private String commentAuthor;

    @JSONField(name = "wp:comment_author_email")
    @PropertyMappingTo("email")
    private String commentAuthorEmail;

    @JSONField(name = "wp:comment_author_url")
    @PropertyMappingTo("authorUrl")
    private String commentAuthorUrl;

    @JSONField(name = "wp:comment_author_IP")
    @PropertyMappingTo("ipAddress")
    private String commentAuthorIp;

    @JSONField(name = "wp:comment_date")
    private String commentDate;

    @JSONField(name = "wp:comment_content")
    @PropertyMappingTo("content")
    private String commentContent;

    @JSONField(name = "wp:comment_approved")
    private String commentApproved;

    @JSONField(name = "wp:comment_parent")
    @PropertyMappingTo("parentId")
    private Long commentParent;

    @JSONField(name = "wp:comment_user_id")
    private String commentUserId;
}
