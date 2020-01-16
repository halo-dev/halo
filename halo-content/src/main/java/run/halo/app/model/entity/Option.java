package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import run.halo.app.model.enums.OptionType;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * option type
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private OptionType type;

    /**
     * option key
     */
    @Column(name = "option_key", columnDefinition = "varchar(100) not null")
    private String key;

    /**
     * option value
     */
    @Column(name = "option_value", columnDefinition = "varchar(1023) not null")
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
        id = null;

        if (type == null) {
            type = OptionType.INTERNAL;
        }
    }
}
