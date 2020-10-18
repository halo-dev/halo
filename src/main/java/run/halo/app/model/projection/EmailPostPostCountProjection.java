package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email post count projection.
 *
 * @author johnniang
 * @date 3/26/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailPostPostCountProjection {

    /**
     * Post count.
     */
    private Long postCount;

    /**
     * Tag id
     */
    private Integer emailId;

}
