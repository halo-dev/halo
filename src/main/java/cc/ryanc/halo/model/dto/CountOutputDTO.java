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

    /**
     * Post count.
     */
    private Long postCount;

    /**
     * Comment count.
     */
    private Long commentCount;

    /**
     * Attachment count.
     */
    private Long attachmentCount;

    /**
     * Establish days
     */
    private Long establishDays;
}
