package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.utils.DateUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * Base entity.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
@ToString
@MappedSuperclass
@EqualsAndHashCode
public class BaseEntity {

    /**
     * Create time.
     */
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * Update time.
     */
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @PrePersist
    protected void prePersist() {
        Date now = DateUtils.now();
        if (createTime == null) {
            createTime = now;
        }

        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updateTime = new Date();
    }

    @PreRemove
    protected void preRemove() {
        updateTime = new Date();
    }

}
