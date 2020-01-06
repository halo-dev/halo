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

    @Column
    private String operatingSystem;

    @Column
    private String operatingSystemVersion;

    @Column
    private String browser;

    @Column
    private String browserVersion;

    @Column
    private String device;

    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private Date time;

    @Column
    private String ip;

    @Column
    private String url;

    @Column
    private String country;

    @Column
    private String region;

    @Column
    private String city;

    @Column
    private String httpMethod;
}
