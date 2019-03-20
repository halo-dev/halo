package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

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
     * 设置项 Key
     */
    @Column(name = "option_key", columnDefinition = "varchar(100) not null")
    private String optionKey;

    /**
     * 设置项 Value
     */
    @Column(name = "option_value", columnDefinition = "varchar(1023) not null")
    private String optionValue;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }
}
