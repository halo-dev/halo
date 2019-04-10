package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import run.halo.app.model.enums.DataType;

import javax.persistence.*;

/**
 * Theme setting entity.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Entity
@Table(name = "theme_settings", indexes = {@Index(columnList = "setting_key")})
@SQLDelete(sql = "update theme_settings set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
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

    @Column(name = "data_type", columnDefinition = "int default 0")
    private DataType type;

    @Override
    protected void prePersist() {
        super.prePersist();

        id = null;

        if (type == null) {
            type = DataType.STRING;
        }
    }
}
