package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import run.halo.app.model.entity.id.VisitorLogId;
import run.halo.app.utils.DateUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * VisitorLog entity.
 *
 * @author Holldean
 */
@Data
@Entity
@Table(name = "visitor_logs", indexes = {@Index(name = "visitor_log_create_time", columnList = "create_time")})
@IdClass(VisitorLogId.class)
@ToString
@EqualsAndHashCode(callSuper = true)
public class VisitorLog extends BaseEntity {

    /**
     * Visitor's access date.
     * Primary key.
     */
    @Id
    @Column(name = "access_date")
    @Temporal(TemporalType.DATE)
    private Date accessDate;

    /**
     * Visitor's ip address.
     * Primary Key.
     */
    @Id
    @Column(name = "ip_address", length = 127)
    private String ipAddress;

    /**
     * Visitor IP's country.
     */
    @Column(name = "country", length = 64)
    private String country;

    /**
     * Visitor IP's province.
     */
    @Column(name = "province", length = 64)
    private String province;

    /**
     * Visitor IP's city.
     */
    @Column(name = "city", length = 64)
    private String city;

    /**
     * Visitor IP's ISP.
     */
    @Column(name = "isp", length = 64)
    private String ISP;

    /**
     * Visitor's visits count.
     */
    @Column(name = "count")
    @ColumnDefault("1")
    private Integer count;

    public VisitorLog() {
    }

    public VisitorLog(String ipAddress) {
        this(null, ipAddress);
    }

    public VisitorLog(Date accessDate, String ipAddress) {
        super();
        this.accessDate = accessDate;
        this.ipAddress = ipAddress;
        this.count = 1;
    }

    @PrePersist
    protected void preCheck() {
        Date now = DateUtils.now();
        if (accessDate == null) {
            accessDate = now;
        }
    }
}