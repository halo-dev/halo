package run.halo.app.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.enums.LogType;

import javax.persistence.*;

/**
 * Log entity.
 *
 * @author johnniang
 */
@Data
@Entity
@Table(name = "logs")
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

        if (logKey == null) {
            logKey = "";
        }

        // Get ip address
        // ###!!! Do not get request IP from here due to asynchronous
        // ipAddress = ServletUtils.getRequestIp();

        if (ipAddress == null) {
            logKey = "";
        }
    }
}
