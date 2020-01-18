package run.halo.app.handler.migrate.support.wordpress;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * <p> wordpress文章项 </p>
 *
 * @author guqing
 * @date 2019-11-17 13:59
 */
@Data
public class Item {
    private String title;

    private String link;

    private String pubDate;

    @JSONField(name = "post_date")
    private String postDate;

    @JSONField(name = "dc:creator")
    private String creator;

    private String description;
    @JSONField(name = "content:encoded")

    private String content;

    @JSONField(name = "wp:comment_status")
    private String commentStatus;

    @JSONField(name = "wp:status")
    private String status;

    @JSONField(name = "wp:post_password")
    private String postPassword;

    @JSONField(name = "wp:is_sticky")
    private Integer isSticky;

    @JSONField(name = "wp:comment")
    private List<Comment> comments;

    @JSONField(name = "category")
    private List<WpCategory> categories;
}
