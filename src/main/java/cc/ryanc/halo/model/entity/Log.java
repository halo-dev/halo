package cc.ryanc.halo.model.entity;


import cc.ryanc.halo.model.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * Log entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "logs")
@SQLDelete(sql = "update logs set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode
public class Log {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * 日志标识
     */
    @Column(name = "log_key", columnDefinition = "varchar(1023) default ''")
    private String logKey;

    /**
     * 日志事件类型
     */
    @Column(name = "type", columnDefinition = "int not null")
    private LogType type;

    /**
     * 日志内容
     */
    @Column(name = "content", columnDefinition = "varchar(1023) not null")
    private String content;

    /**
     * 操作 IP
     */
    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;

    /**
     * 创建时间戳
     */
    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 更新时间戳
     */
    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;
}
