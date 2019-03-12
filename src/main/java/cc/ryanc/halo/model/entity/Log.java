package cc.ryanc.halo.model.entity;


import cc.ryanc.halo.model.entity.enums.LogType;
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

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "tinyint default 0")
    private Boolean deleted;

    @Column(name = "log_key", columnDefinition = "varchar(1023) default ''")
    private String logKey;

    @Column(name = "type", columnDefinition = "int not null")
    private LogType type;

    @Column(name = "content", columnDefinition = "varchar(1023) not null")
    private String content;

    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;

}
