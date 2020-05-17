package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Visitor's ip log post count by region.
 *
 * @author Holldean
 * @date 2020-5-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorLogRegionCountProjection {

    private String region;

    private Long count;

}
