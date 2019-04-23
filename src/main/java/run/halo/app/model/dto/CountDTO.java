package run.halo.app.model.dto;

import lombok.Data;

/**
 * Count output DTO.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class CountDTO {

    private long postCount;

    private long commentCount;

    private long attachmentCount;

    private long establishDays;

    private long linkCount;

    private long visitCount;

    private long likeCount;
}
