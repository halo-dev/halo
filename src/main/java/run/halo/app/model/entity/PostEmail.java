package run.halo.app.model.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Post email entity.
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name = "post_emails", indexes = {
        @Index(name = "post_emails_post_id", columnList = "post_id"),
        @Index(name = "post_emails_email_id", columnList = "email_id")})
public class PostEmail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Post id.
     */
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    /**
     * Email id.
     */
    @Column(name = "email_id", nullable = false)
    private Integer emailId;

    /**
     * Email name.
     */
    @Column(name = "email_name", nullable = false)
    private String emailName;

    /**
     * Email value.
     */
    @Column(name = "email_value", length = 127, nullable = false)
    private String emailValue;


    /**
     * Rendered content.
     */
    @Column(name = "post_format_content")
    @Lob
    private String postFormatContent;

    /**
     * Original content,not format.
     */
    @Column(name = "post_original_content", nullable = false)
    @Lob
    private String postOriginalContent;

    /**
     * Post title.
     */
    @Column(name = "post_title", nullable = false)
    private String postTitle;

    /**
     * Post slug.
     */
    @Column(name = "post_slug", unique = true)
    private String postSlug;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (postOriginalContent == null) {
            postOriginalContent = "";
        }

        if (postFormatContent == null) {
            postFormatContent = "";
        }

        PostEmail postEmail = (PostEmail) o;
        return Objects.equals(postId, postEmail.postId) &&
                Objects.equals(emailId, postEmail.emailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, emailId);
    }
}
