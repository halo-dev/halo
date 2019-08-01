package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Category with post count dto.
 *
 * @author johnniang
 * @date 19-4-23
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryWithPostCountDTO extends CategoryDTO {

    private Long postCount;
}
