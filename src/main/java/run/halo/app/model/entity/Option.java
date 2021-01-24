package run.halo.app.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.OptionType;

/**
 * Setting entity.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-20
 */
@Data
@Entity
@Table(name = "options")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id",
        strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * option type
     */
    @Column(name = "type")
    @ColumnDefault("0")
    private OptionType type;

    /**
     * option key
     */
    @Column(name = "option_key", length = 100, nullable = false)
    private String key;

    /**
     * option value
     */
    @Column(name = "option_value", nullable = false)
    @Lob
    private String value;

    public Option(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Option(OptionType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    @Override
    public void prePersist() {
        super.prePersist();

        if (type == null) {
            type = OptionType.INTERNAL;
        }
    }
}
