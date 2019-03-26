package cc.ryanc.halo.model.projection;

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
public class TagPostCountProjection {

    /**
     * Post count.
     */
    private Long count;

    /**
     * Tag id
     */
    private Integer tagId;

}
