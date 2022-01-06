package run.halo.app.model.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.PostStatus;

/**
 * Content patch log entity.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "content_patch_logs")
public class ContentPatchLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support"
        + ".CustomIdGenerator")
    private Integer id;

    private Integer postId;

    @Lob
    private String contentDiff;

    @Lob
    private String originalContentDiff;

    @Column(name = "version", nullable = false)
    private Integer version;

    @ColumnDefault("1")
    @Column(name = "status")
    private PostStatus status;

    @Temporal(TemporalType.TIMESTAMP)
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
