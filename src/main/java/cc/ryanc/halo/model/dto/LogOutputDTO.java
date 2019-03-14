package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Log;
import cc.ryanc.halo.model.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
public class LogOutputDTO implements OutputConverter<LogOutputDTO, Log> {

    /**
     * Log id.
     */
    private Long id;

    /**
     * 日志标识
     */
    private String logKey;

    /**
     * 日志事件类型
     */
    private LogType type;

    /**
     * 日志内容
     */
    private String content;

    /**
     * 操作 IP
     */
    private String ipAddress;

    /**
     * 创建时间戳
     */
    private Date createTime;
}
