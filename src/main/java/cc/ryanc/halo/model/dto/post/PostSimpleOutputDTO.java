package cc.ryanc.halo.model.dto.post;

import cc.ryanc.halo.model.enums.PostCreateFrom;
import cc.ryanc.halo.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Page simple output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostSimpleOutputDTO extends PostMinimalOutputDTO {

    private PostType type;

    private String summary;

    private String thumbnail;

    private Long visits;

    private Boolean disallowComment;

    private String template;

    private Integer topPriority;

    private PostCreateFrom createFrom;

    private Long likes;
}
