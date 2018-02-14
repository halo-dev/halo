package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : RYAN0UP
 * @date : 2018/1/10
 * @version : 1.0
 * description :
 */
@Data
@Entity
@Table(name = "halo_attachment")
public class Attachment implements Serializable{

    /**
     * 附件编号
     */
    @Id
    @GeneratedValue
    private Long attachId;

    /**
     * 附件名
     */
    private String attachName;

    /**
     * 附件路径
     */
    private String attachPath;

    /**
     * 附件缩略图路径
     */
    private String attachSmallPath;

    /**
     * 附件类型
     */
    private String attachType;

    /**
     * 附件后缀
     */
    private String attachSuffix;

    /**
     * 上传时间
     */
    private Date attachCreated;
}
