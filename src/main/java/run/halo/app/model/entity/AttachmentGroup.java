package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Attachment group entity.
 * @author guqing
 * @date 2020-10-24
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
@Table(name = "attachment_groups", indexes = {@Index(name = "attachment_groups_parent_id", columnList = "parent_id")})
public class AttachmentGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Attachment parent group id.
     */
    @Column(name = "parent_id")
    @ColumnDefault("0")
    private Integer parentId;

    /**
     * Attachment group name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    @Override
    public void prePersist() {
        super.prePersist();

        if (parentId == null) {
            parentId = 0;
        }
    }
}
