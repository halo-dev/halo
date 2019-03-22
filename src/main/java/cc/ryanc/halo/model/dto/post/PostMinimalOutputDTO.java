package cc.ryanc.halo.model.dto.post;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * Post minimal output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode
public class PostMinimalOutputDTO implements OutputConverter<PostMinimalOutputDTO, Post> {

    /**
     * Post id.
     */
    private Integer id;

    /**
     * Post title
     */
    private String title;

    /**
     * Post status.
     */
    private PostStatus status;

    /**
     * Post url.
     */
    private String url;

    /**
     * Post type
     */
    private PostType type;

    /**
     * 更新时间戳
     */
    private Date updateTime;

    /**
     * 创建时间戳
     */
    private Date createTime;

    /**
     * Edit time.
     */
    private Date editTime;
}
