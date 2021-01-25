package run.halo.app.model.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

/**
 * Photo entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "photos", indexes = {
    @Index(name = "photos_team", columnList = "team"),
    @Index(name = "photos_create_time", columnList = "create_time")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id",
        strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Picture name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Picture description.
     */
    @Column(name = "description")
    private String description;

    /**
     * Shooting time / creation time.
     */
    @Column(name = "take_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date takeTime;

    /**
     * Picture location.
     */
    @Column(name = "location")
    private String location;

    /**
     * Thumbnail
     */
    @Column(name = "thumbnail", length = 1023)
    private String thumbnail;

    /**
     * Picture access path.
     */
    @Column(name = "url", length = 1023, nullable = false)
    private String url;

    /**
     * Photo team name.
     */
    @Column(name = "team")
    private String team;

    @Override
    public void prePersist() {
        super.prePersist();

        if (takeTime == null) {
            takeTime = this.getCreateTime();
        }

        if (description == null) {
            description = "";
        }

        if (location == null) {
            location = "";
        }

        if (thumbnail == null) {
            thumbnail = "";
        }

        if (team == null) {
            team = "";
        }
    }
}
