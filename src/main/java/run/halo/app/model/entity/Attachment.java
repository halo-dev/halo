package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import run.halo.app.model.enums.AttachmentType;

import javax.persistence.*;

/**
 * Attachment entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "attachments",
    indexes = {@Index(name = "attachments_media_type", columnList = "media_type"),
        @Index(name = "attachments_create_time", columnList = "create_time")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Attachment name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Attachment access path.
     */
    @Column(name = "path", length = 1023, nullable = false)
    private String path;

    /**
     * File key: oss file key or local file key (Just for deleting)
     */
    @Column(name = "file_key", length = 2047)
    private String fileKey;

    /**
     * Thumbnail access path.
     */
    @Column(name = "thumb_path", length = 1023)
    private String thumbPath;

    /**
     * Attachment media type.
     */
    @Column(name = "media_type", length = 127, nullable = false)
    private String mediaType;

    /**
     * Attachment suffix,such as png, zip, mp4, jpge.
     */
    @Column(name = "suffix", length = 50)
    private String suffix;

    /**
     * Attachment width.
     */
    @Column(name = "width")
    @ColumnDefault("0")
    private Integer width;

    /**
     * Attachment height.
     */
    @Column(name = "height")
    @ColumnDefault("0")
    private Integer height;

    /**
     * Attachment size.
     */
    @Column(name = "size", nullable = false)
    private Long size;

    /**
     * Attachment upload type,LOCAL,UPYUN or QNYUN.
     */
    @Column(name = "type")
    @ColumnDefault("0")
    private AttachmentType type;

    @Override
    public void prePersist() {
        super.prePersist();

        if (fileKey == null) {
            fileKey = "";
        }

        if (thumbPath == null) {
            thumbPath = "";
        }

        if (suffix == null) {
            suffix = "";
        }

        if (width == null) {
            width = 0;
        }

        if (height == null) {
            height = 0;
        }

        if (type == null) {
            type = AttachmentType.LOCAL;
        }
    }
}
