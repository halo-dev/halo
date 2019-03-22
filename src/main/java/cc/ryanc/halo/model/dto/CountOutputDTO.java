package cc.ryanc.halo.model.dto;

import lombok.Data;

/**
 * Count output DTO.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class CountOutputDTO {

    private Long postCount;

    private Long commentCount;

    private Long attachmentCount;

    private Long establishDays;
}
