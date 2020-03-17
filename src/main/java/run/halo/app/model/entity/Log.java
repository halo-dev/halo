package run.halo.app.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.LogType;

import javax.persistence.*;

/**
 * Log entity.
 *
 * @author johnniang
 */
@Data
@Entity
@Table(name = "logs", indexes = {@Index(name = "logs_create_time", columnList = "create_time")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Log extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Long id;

    /**
     * Log key.
     */
    @Column(name = "log_key", length = 1023)
    private String logKey;

    /**
     * Log type.
     */
    @Column(name = "type", nullable = false)
    private LogType type;

    /**
     * Log content.
     */
    @Column(name = "content", length = 1023, nullable = false)
    private String content;

    /**
     * Operator's ip address.
     */
    @Column(name = "ip_address", length = 127)
    private String ipAddress;


    @Override
    public void prePersist() {
        super.prePersist();

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
