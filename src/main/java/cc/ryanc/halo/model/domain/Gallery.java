package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <pre>
 *     相册
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/2/26
 */
@Data
@Entity
@Table(name = "halo_gallery")
public class Gallery implements Serializable {

    private static final long serialVersionUID = 1646093266970933841L;

    /**
     * 图片编号
     */
    @Id
    @GeneratedValue
    private Long galleryId;

    /**
     * 图片名称
     */
    private String galleryName;

    /**
     * 图片描述
     */
    private String galleryDesc;

    /**
     * 图片日期/拍摄日期
     */
    private String galleryDate;

    /**
     * 图片拍摄地点
     */
    private String galleryLocation;

    /**
     * 图片缩略图地址
     */
    private String galleryThumbnailUrl;

    /**
     * 图片地址
     */
    private String galleryUrl;
}
