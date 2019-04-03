package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Log;
import run.halo.app.model.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Log;
import run.halo.app.model.enums.LogType;

import java.util.Date;

/**
 * @author johnniang
 * @date 3/19/19
 */
@Data
@ToString
@EqualsAndHashCode
public class LogOutputDTO implements OutputConverter<LogOutputDTO, Log> {

    private Long id;

    private String logKey;

    private LogType type;

    private String content;

    private String ipAddress;

    private Date createTime;
}
