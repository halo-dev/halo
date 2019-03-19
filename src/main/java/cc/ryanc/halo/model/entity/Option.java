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
@EqualsAndHashCode
public class Option {

    @Id
    @GeneratedValue
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

    /**
     * 创建时间戳
     */
    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 更新时间戳
     */
    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;

    @PrePersist
    public void prePersist() {
        id = null;
    }

    @PreUpdate
    public void preUpdate() {
        if (updateTime == null) {
            updateTime = DateUtils.now();
        }
    }
}
