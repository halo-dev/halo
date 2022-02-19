package run.halo.app.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.Assert;
import run.halo.app.model.enums.PostStatus;

/**
 * Post content.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Data
@Entity
@Table(name = "contents")
@EqualsAndHashCode(callSuper = true)
public class Content extends BaseEntity {

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

        /**
         * Create {@link PatchedContent} from {@link Content}.
         *
         * @param content a {@link Content} must not be null
         */
        public PatchedContent(Content content) {
            Assert.notNull(content, "The content must not be null.");
            this.content = content.getContent();
            this.originalContent = content.getOriginalContent();
        }

        public static PatchedContent of(Content postContent) {
            return new PatchedContent(postContent);
        }
    }

}
