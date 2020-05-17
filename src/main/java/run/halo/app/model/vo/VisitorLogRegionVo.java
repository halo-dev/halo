package run.halo.app.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.model.projection.VisitorLogRegionCountProjection;

import java.util.List;

/**
 * VisitorLog Vo.
 *
 * @author Holldean
 * @date 2020-5-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorLogRegionVo {

    private List<VisitorLogRegionCountProjection> countByCountry;

    private List<VisitorLogRegionCountProjection> countByProvince;

}
