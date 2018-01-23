package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description : 友情链接的实体类
 */
@Data
@Entity
@Table(name = "halo_link")
public class Link implements Serializable{
    @Id
    @GeneratedValue
    private Long linkId;
    private String linkName;
    private String linkUrl;
    private String linkPic;
    private String linkDesc;
}
