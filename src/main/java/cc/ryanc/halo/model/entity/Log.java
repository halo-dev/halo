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
     * Log key.
     */
    @Column(name = "log_key", columnDefinition = "varchar(1023) default ''")
    private String logKey;

    /**
     * Log type.
     */
    @Column(name = "type", columnDefinition = "int not null")
    private LogType type;

    /**
     * Log content.
     */
    @Column(name = "content", columnDefinition = "varchar(1023) not null")
    private String content;

    /**
     * Operator's ip address.
     */
    @Column(name = "ip_address", columnDefinition = "varchar(127) default ''")
    private String ipAddress;


    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }
}
