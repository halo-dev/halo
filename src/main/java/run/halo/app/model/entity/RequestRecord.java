package run.halo.app.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * RequestRecord entity.
 *
 * @author ijkzen
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
public class RequestRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operatingSystem", columnDefinition = "varchar(255) not null")
    private String operatingSystem;

    @Column(name = "operatingSystemVersion", columnDefinition = "varchar(255) not null")
    private String operatingSystemVersion;

    @Column(name = "browser", columnDefinition = "varchar(255) not null")
    private String browser;

    @Column(name = "browserVersion", columnDefinition = "varchar(255) not null")
    private String browserVersion;

    @Column(name = "device", columnDefinition = "varchar(255) not null")
    private String device;

    @Column(name = "time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private Date time;

    @Column(name = "ip", columnDefinition = "varchar(255) not null")
    private String ip;

    @Column(name = "url", columnDefinition = "varchar(255) not null")
    private String url;

    @Column(name = "country", columnDefinition = "varchar(255) not null")
    private String country;

    @Column(name = "region", columnDefinition = "varchar(255) not null")
    private String region;

    @Column(name = "city", columnDefinition = "varchar(255) not null")
    private String city;

    @Column(name = "httpMethod", columnDefinition = "varchar(255) not null")
    private String httpMethod;
}
