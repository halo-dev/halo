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
 * @version : 1.0
 * @date : 2018/1/10
 * description :
 */
@Data
@Entity
@Table(name = "halo_attachment")
public class Attachment implements Serializable{
    @Id
    @GeneratedValue
    private Integer attachId;
    private String attachName;
    private String attachPath;
    private String attachSmallPath;
    private String attachType;
    private String attachSuffix;
    private Date attachCreated;
}
