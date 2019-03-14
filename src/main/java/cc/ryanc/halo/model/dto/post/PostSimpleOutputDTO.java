package cc.ryanc.halo.model.dto.post;

import cc.ryanc.halo.model.enums.PostCreateFrom;
import cc.ryanc.halo.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * Page simple output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostSimpleOutputDTO extends PostWithTitleDTO {

    /**
     * 文章类型
     * 0: 普通文章
     * 1: 自定义页面
     * 2: 日志
     */
    private PostType type;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 浏览量
     */
    private Long visits;

    /**
     * 是否允许评论
     */
    private Boolean disallowComment;

    /**
     * 文章密码
     */
    private String password;

    /**
     * 自定义渲染模板名称
     */
    private String template;

    /**
     * 是否置顶
     */
    private Integer topPriority;

    /**
     * 发布来源
     */
    private PostCreateFrom createFrom;

    /**
     * 点赞量/喜欢量
     */
    private Long likes;

    /**
     * 创建时间戳
     */
    private Date createTime;

    /**
     * 更新时间戳
     */
    private Date updateTime;

    /**
     * Edit time.
     */
    private Date editTime;
}
