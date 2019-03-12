package cc.ryanc.halo.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Attachment entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    /**
     * 附件名称
     */
    @Column(name = "name",columnDefinition = "varchar(255) not null")
    private String name;

    /**
     * 附件路径
     */
    @Column(name = "path",columnDefinition = "varchar(1023) default ''")
    private String path;

    /**
     * 缩略图路径
     */
    @Column(name = "thumb_path",columnDefinition = "varchar(1023) default ''")
    private String thumbPath;

    /**
     * 附件类型
     */
    @Column(name = "media_type",columnDefinition = "varchar(50) default ''")
    private String mediaType;

    /**
     * 附件后缀
     */
    @Column(name = "suffix",columnDefinition = "varchar(50) default ''")
    private String suffix;

    /**
     * 附件尺寸
     */
    @Column(name = "dimension",columnDefinition = "varchar(50) default ''")
    private String dimension;

    /**
     * 附件大小
     */
    @Column(name = "size",columnDefinition = "varchar(50) default ''")
    private String size;

    /**
     * 附件上传类型
     */
    @Column(name = "type",columnDefinition = "int default 0")
    private Integer type;

    /**
     * 创建时间戳
     */
    @Column(name = "create_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 更新时间戳
     */
    @Column(name = "update_time", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", columnDefinition = "TINYINT default 0")
    private Boolean deleted;
}
