package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Setting entity.
 *
 * @author johnniang
 */
@Entity
@Table(name = "options")
@SQLDelete(sql = "update options set deleted = true where id = ?")
@Where(clause = "deleted = false")
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * option key
     */
    @Column(name = "option_key", columnDefinition = "varchar(100) not null")
    private String optionKey;

    /**
     * option value
     */
    @Column(name = "option_value", columnDefinition = "varchar(1023) not null")
    private String optionValue;

    /**
     * source,default is system
     */
    @Column(name = "source", columnDefinition = "varchar(127) default 'system'")
    private String source;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
        source = "system";
    }
}
