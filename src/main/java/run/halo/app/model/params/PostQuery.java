package run.halo.app.model.params;

import java.util.Set;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.enums.PostStatus;

/**
 * Post query.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Data
public class PostQuery {

    /**
     * Keyword.
     */
    private String keyword;

    /**
     * Post status.
     */
    private Set<PostStatus> status;

    /**
     * Category id.
     */
    private Integer categoryId;

    public void setStatus(PostStatus... status) {
        if (!CollectionUtils.isEmpty(this.status)) {
            throw new IllegalArgumentException("There is already a value in the statusSet, "
                + "If you want to overwrite please use the overload method.");
        }
        this.status = Set.of(status);
    }

    public void setStatus(Set<PostStatus> status) {
        this.status = status;
    }
}
