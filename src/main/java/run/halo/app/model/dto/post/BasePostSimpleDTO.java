package run.halo.app.model.dto.post;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.enums.PostCreateFrom;

/**
 * Base page simple output dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BasePostSimpleDTO extends BasePostMinimalDTO {

    private String summary;

    private String thumbnail;

    private Long visits = 0L;

    private Boolean disallowComment;

    private String password;

    private String template;

    private Integer topPriority = 0;

    private PostCreateFrom createFrom;

    private Long likes = 0L;
}
