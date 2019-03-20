package cc.ryanc.halo.model.dto;

import lombok.Data;

/**
 * Tag with post count output dto.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
public class TagWithCountOutputDTO extends TagOutputDTO {

    private Long postCount;

}
