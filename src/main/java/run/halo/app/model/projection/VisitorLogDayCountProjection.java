package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Visitor's ip log post count by day.
 *
 * @author Holldean
 * @date 2020-5-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorLogDayCountProjection {

    private Date date;

    private Long count;
}
