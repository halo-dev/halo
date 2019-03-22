package cc.ryanc.halo.model.entity;


import cc.ryanc.halo.model.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

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
@EqualsAndHashCode(callSuper = true)
public class Log extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }
}
