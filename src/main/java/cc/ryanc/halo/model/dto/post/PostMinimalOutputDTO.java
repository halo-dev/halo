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

    private Integer id;

    private String title;

    private PostStatus status;

    private String url;

    private PostType type;

    private Date updateTime;

    private Date createTime;

    private Date editTime;
}
