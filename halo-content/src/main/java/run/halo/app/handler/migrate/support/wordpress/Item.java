package run.halo.app.handler.migrate.support.wordpress;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import run.halo.app.handler.migrate.utils.PropertyMappingTo;
import run.halo.app.model.entity.BasePost;

import java.util.List;

/**
 * <p> WordPress导出的xml中对于的item子节点的值将会被映射到该类的属性上,最终被解析为文章属性{@link BasePost} </p>
 *
 * @author guqing
 * @author ryanwang
 * @date 2019-11-17 13:59
 */
@Data
public class Item {

    private String title;

    private String pubDate;

    private String description;

    @JSONField(name = "content:encoded")
    @PropertyMappingTo("formatContent")
    private String content;

    @JSONField(name = "excerpt:encoded")
    @PropertyMappingTo("summary")
    private String excerpt;

    @JSONField(name = "wp:post_date")
    private String postDate;

    @JSONField(name = "wp:comment_status")
    private String commentStatus;

    @JSONField(name = "wp:post_name")
    @PropertyMappingTo("url")
    private String postName;

    @JSONField(name = "wp:status")
    private String status;

    @JSONField(name = "wp:post_password")
    @PropertyMappingTo("password")
    private String postPassword;

    @JSONField(name = "wp:is_sticky")
    @PropertyMappingTo("topPriority")
    private Integer isSticky;

    @JSONField(name = "wp:comment")
    private List<Comment> comments;

    @JSONField(name = "category")
    private List<WpCategory> categories;
}
