package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description : 用户详细信息实体类
 */
@Data
@Entity
@Table(name = "halo_usermeta")
public class UserMeta implements Serializable{
    @Id
    @GeneratedValue
    private Long userMetaId;
    private Long userId;
    private String userMetaKey;
    @Lob
    private String userMetaValue;
}
