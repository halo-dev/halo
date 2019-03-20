package cc.ryanc.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;

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
    @Column(name = "path", columnDefinition = "varchar(1023) default ''")
    private String path;

    /**
     * 缩略图路径
     */
    @Column(name = "thumb_path", columnDefinition = "varchar(1023) default ''")
    private String thumbPath;

    /**
     * 附件类型
     */
    @Column(name = "media_type", columnDefinition = "varchar(50) default ''")
    private String mediaType;

    /**
     * 附件后缀
     */
    @Column(name = "suffix", columnDefinition = "varchar(50) default ''")
    private String suffix;

    /**
     * 附件尺寸
     */
    @Column(name = "dimension", columnDefinition = "varchar(50) default ''")
    private String dimension;

    /**
     * 附件大小
     */
    @Column(name = "size", columnDefinition = "varchar(50) default ''")
    private String size;

    /**
     * 附件上传类型
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private Integer type;

    @Override
    public void prePersist() {
        super.prePersist();
        id = null;
    }

}
