package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Category post count projection.
 *
 * @author johnniang
 * @date 19-4-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPostCountProjection {

    private Long postCount;

    private Integer categoryId;
}
