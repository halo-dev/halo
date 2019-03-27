package cc.ryanc.halo.model.entity;

import cc.ryanc.halo.model.enums.AttachmentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Attachment entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "attachments")
@SQLDelete(sql = "update attachments set deleted = true where id = ?")
@Where(clause = "deleted = false")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 附件名称
     */
    @Column(name = "name", columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 附件路径
     */
    @Column(name = "path", columnDefinition = "varchar(1023) not null")
    private String path;

    /**
     * File key: oss file key or local file key (Just for deleting)
     */
    @Column(name = "file_key", columnDefinition = "varchar(2047) default ''")
    private String fileKey;

    /**
     * 缩略图路径
     */
    @Column(name = "thumb_path", columnDefinition = "varchar(1023) default ''")
    private String thumbPath;

    /**
     * 附件类型
     */
    @Column(name = "media_type", columnDefinition = "varchar(50) not null")
    private String mediaType;

    /**
     * 附件后缀
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
     * 附件大小
     */
    @Column(name = "size", columnDefinition = "bigint not null")
    private Long size;

    /**
     * 附件上传类型
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private AttachmentType type;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;

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
            type = AttachmentType.SERVER;
        }
    }
}
