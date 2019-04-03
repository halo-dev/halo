package run.halo.app.model.dto.post;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.PostType;

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
