package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * Photo entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "photos")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Picture name.
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * Picture description.
     */
    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    /**
     * Shooting time / creation time.
     */
    @Column(name = "take_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date takeTime;

    /**
     * Picture location.
     */
    @Column(name = "location", columnDefinition = "varchar(255) default ''")
    private String location;

    /**
     * Thumbnail
     */
    @Column(name = "thumbnail", columnDefinition = "varchar(1023) default ''")
    private String thumbnail;

    /**
     * Picture access path.
     */
    @Column(name = "url", columnDefinition = "varchar(1023) not null")
    private String url;

    /**
     * Photo team name.
     */
    @Column(name = "team", columnDefinition = "varchar(255) default ''")
    private String team;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;

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
