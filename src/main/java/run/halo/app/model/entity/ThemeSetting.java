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
    @Column(name = "setting_key", nullable = false)
    private String key;

    /**
     * Setting value
     */
    @Column(name = "setting_value", nullable = false)
    private String value;

    /**
     * Theme id.
     */
    @Column(name = "theme_id", nullable = false)
    private String themeId;
}
