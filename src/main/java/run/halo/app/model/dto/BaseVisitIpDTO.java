package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.BaseVisitIp;

import java.util.Date;

/**
 * Base visit ip address dto.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Data
@ToString
@EqualsAndHashCode
public class BaseVisitIpDTO implements OutputConverter<BaseVisitIpDTO, BaseVisitIp> {

    private Integer id;

    private String requestIp;

    private Date requestTime;
}
