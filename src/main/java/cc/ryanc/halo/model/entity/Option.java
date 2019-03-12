package cc.ryanc.halo.model.entity;

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
@EqualsAndHashCode
public class Option {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;

    @Column(name = "option_key", columnDefinition = "varchar(100) not null")
    private String optionKey;

    @Column(name = "option_value", columnDefinition = "varchar(1023) not null")
    private String optionValue;

}
