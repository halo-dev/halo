package run.halo.app.model.dto;

import lombok.Data;

/**
 * Statistic DTO.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class StatisticDTO {

    private long postCount;

    private long commentCount;

    private long attachmentCount;

    private long birthday;

    private long establishDays;

    private long linkCount;

    private long visitCount;

    private long likeCount;
}
