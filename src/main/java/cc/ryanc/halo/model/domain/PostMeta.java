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
 * description : 文章详细信息实体类
 */
@Data
@Entity
@Table(name = "halo_postmeta")
public class PostMeta implements Serializable{
    @Id
    @GeneratedValue
    private Integer metaId;

}
