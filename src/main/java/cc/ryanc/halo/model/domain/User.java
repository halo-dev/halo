package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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

    private static final long serialVersionUID = -5144055068797033748L;

    /**
     * 编号
     */
    @Id
    @GeneratedValue
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

    /**
     * 是否禁用登录
     */
    private String loginEnable;

    /**
     * 最后一次登录时间
     */
    private Date loginLast;

    /**
     * 登录错误次数记录
     */
    private Integer loginError;
}
