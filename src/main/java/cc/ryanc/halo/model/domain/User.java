package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description : 用户实体类
 */
@Data
@Entity
@Table(name = "halo_user")
public class User implements Serializable{

    @Id
    @GeneratedValue
    /**
     * 编号
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 显示名称
     */
    private String userDisplayName;
    /**
     * 密码
     */
    private String userPass;
    /**
     * 邮箱
     */
    private String userEmail;
    /**
     * 头像
     */
    private String userAvatar;
    /**
     * 说明
     */
    private String userDesc;
}
