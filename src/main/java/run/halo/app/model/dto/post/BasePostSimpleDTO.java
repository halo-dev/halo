package run.halo.app.model.dto.post;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    private Long visits;

    private Boolean disallowComment;

    private String password;

    private String template;

    private Integer topPriority;

    private Long likes;

    private Long wordCount;

    private Boolean inProgress;

    public boolean isTopped() {
        return this.topPriority != null && this.topPriority > 0;
    }
}
