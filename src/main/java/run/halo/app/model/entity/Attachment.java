package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
@Table(name = "attachments")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Attachment name.
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * Attachment access path.
     */
    @Column(name = "path", columnDefinition = "varchar(1023) not null")
    private String path;

    /**
     * File key: oss file key or local file key (Just for deleting)
     */
    @Column(name = "file_key", columnDefinition = "varchar(2047) default ''")
    private String fileKey;

    /**
     * Thumbnail access path.
     */
    @Column(name = "thumb_path", columnDefinition = "varchar(1023) default ''")
    private String thumbPath;

    /**
     * Attachment media type.
     */
    @Column(name = "media_type", columnDefinition = "varchar(127) not null")
    private String mediaType;

    /**
     * Attachment suffix,such as png, zip, mp4, jpge.
     */
    @Column(name = "suffix", columnDefinition = "varchar(50) default ''")
    private String suffix;

    /**
     * Attachment width.
     */
    @Column(name = "width", columnDefinition = "int default 0")
    private Integer width;

    /**
     * Attachment height.
     */
    @Column(name = "height", columnDefinition = "int default 0")
    private Integer height;

    /**
     * Attachment size.
     */
    @Column(name = "size", columnDefinition = "bigint not null")
    private Long size;

    /**
     * Attachment upload type,LOCAL,UPYUN or QNYUN.
     */
    @Column(name = "type", columnDefinition = "int default 0")
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
