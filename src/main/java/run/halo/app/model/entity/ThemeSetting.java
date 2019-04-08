package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Theme setting entity.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Entity
@Table(name = "theme_settings", indexes = {@Index(columnList = "theme"), @Index(columnList = "setting_key")})
@SQLDelete(sql = "update theme_settings set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ThemeSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "theme", columnDefinition = "varchar(255) not null")
    private String theme;

    @Column(name = "setting_key", columnDefinition = "varchar(255) not null")
    private String key;

    @Column(name = "setting_value", columnDefinition = "varchar(10239) not null")
    private String value;

}
