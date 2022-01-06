package run.halo.app.model.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.Assert;
import run.halo.app.model.enums.PostStatus;

/**
 * Post content.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "contents")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER,
    columnDefinition = "int default 0")
public class BaseContent extends BaseEntity {

    @Id
    @Column(name = "post_id")
    private Integer id;

    @Column(name = "status")
    @ColumnDefault("1")
    private PostStatus status;

    /**
     * PatchLog from which the current content comes.
     */
    private Integer patchLogId;

    /**
     * <p>The patch log head that the current content points to.</p>
     * <pre>
     *     \-v1-v2-v3(HEAD)-v5(draft)
     *           \-v4
     * </pre>
     * e.g. The latest version is v4.
     * <li>At this time, I switch to V3, and the head points to v3.
     * <li>When creating a draft (V5) in V3, the head points to V5,
     * but the <code>patchLogId</code> of the current content still points to the record
     * where V3 is located
     */
    private Integer headPatchLogId;

    @Lob
    private String content;

    @Lob
    private String originalContent;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
            o)) {
            return false;
        }
        BaseContent content = (BaseContent) o;
        return id != null && Objects.equals(id, content.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    protected void prePersist() {
        super.prePersist();

        if (originalContent == null) {
            originalContent = "";
        }

        if (content == null) {
            content = "";
        }
    }

    /**
     * V1 based content differentiation.
     *
     * @author guqing
     * @since 2021-12-20
     */
    @Data
    public static class ContentDiff {

        private String diff;

        private String originalDiff;
    }

    /**
     * The actual content of the post obtained by applying patch to V1 version.
     *
     * @author guqing
     * @since 2021-12-20
     */
    @Data
    public static class PatchedContent {

        private String content;

        private String originalContent;

        public PatchedContent() {
        }

        public PatchedContent(String content, String originalContent) {
            this.content = content;
            this.originalContent = originalContent;
        }

        public PatchedContent(BaseContent content) {
            Assert.notNull(content, "The content must not be null.");
            this.content = content.getContent();
            this.originalContent = content.getOriginalContent();
        }
    }

}
