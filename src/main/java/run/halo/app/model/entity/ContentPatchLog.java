package run.halo.app.model.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.PostStatus;

/**
 * Content patch log entity.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "content_patch_logs", indexes = {
    @Index(name = "idx_post_id", columnList = "post_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_version", columnList = "version")})
public class ContentPatchLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support"
        + ".CustomIdGenerator")
    private Integer id;

    @Column(name = "post_id")
    private Integer postId;

    @Lob
    @Column(name = "content_diff")
    private String contentDiff;

    @Lob
    @Column(name = "original_content_diff")
    private String originalContentDiff;

    @Column(name = "version", nullable = false)
    private Integer version;

    @ColumnDefault("1")
    @Column(name = "status")
    private PostStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publish_time")
    private Date publishTime;

    /**
     * Current version of the source patch log id, default value is 0.
     */
    @Column(name = "source_id", nullable = false)
    private Integer sourceId;

    @Override
    protected void prePersist() {
        super.prePersist();
        if (version == null) {
            version = 1;
        }

        if (sourceId == null) {
            sourceId = 0;
        }
    }
}
