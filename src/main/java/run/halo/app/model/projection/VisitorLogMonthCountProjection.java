package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Visitor's ip log post count by month.
 *
 * @author Holldean
 * @date 2020-5-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorLogMonthCountProjection {

    private Integer month;

    private Long count;
}
