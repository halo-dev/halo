package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag post count projection.
 *
 * @author johnniang
 * @date 3/26/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagPostPostCountProjection {

    /**
     * Post count.
     */
    private Long postCount;

    /**
     * Tag id
     */
    private Integer tagId;

}
