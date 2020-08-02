package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Theme setting entity.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Data
@Entity
@Table(name = "theme_settings", indexes = {
        @Index(name = "theme_settings_setting_key", columnList = "setting_key"),
        @Index(name = "theme_settings_theme_id", columnList = "theme_id")})
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ThemeSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
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
    @Lob
    private String value;

    /**
     * Theme id.
     */
    @Column(name = "theme_id", nullable = false)
    private String themeId;
}
