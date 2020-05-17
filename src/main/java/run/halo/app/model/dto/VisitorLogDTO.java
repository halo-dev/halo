package run.halo.app.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.VisitorLog;

import java.util.Date;

/**
 * @author Holldean
 * @date 2020-5-15
 */
@Data
@ToString
@EqualsAndHashCode
public class VisitorLogDTO implements OutputConverter<VisitorLogDTO, VisitorLog> {

    private Date updateTime;

    private Date accessDate;

    private String ipAddress;

    private String country;

    private String province;

    private String city;

    private String ISP;

    private Integer count;

}
