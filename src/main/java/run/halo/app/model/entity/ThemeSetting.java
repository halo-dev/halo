package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * Theme setting entity.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Data
@Entity
@Table(name = "theme_settings")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ThemeSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Setting key.
     */
    @Column(name = "setting_key", columnDefinition = "varchar(255) not null")
    private String key;

    /**
     * Setting value
     */
    @Column(name = "setting_value", columnDefinition = "varchar(10239) not null")
    private String value;

    /**
     * Theme id.
     */
    @Column(name = "theme_id", columnDefinition = "varchar(255) not null")
    private String themeId;

    @Override
    protected void prePersist() {
        super.prePersist();

        id = null;
    }
}
