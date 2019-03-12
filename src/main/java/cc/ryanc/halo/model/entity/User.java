package cc.ryanc.halo.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * User entity
 *
 * @author : RYAN0UP
 * @date : 2019-03-12
 */
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    /**
     * 用户名
     */
    @Column(name = "username",columnDefinition = "varchar(50) not null")
    private String username;

    /**
     * 昵称
     */
    @Column(name = "nickname",columnDefinition = "varchar(255) not null")
    private String nickname;

    /**
     * 密码
     */
    @Column(name = "password",columnDefinition = "varchar(255) not null")
    private String password;

    /**
     * 邮箱
     */
    @Column(name = "email",columnDefinition = "varchar(127) default ''")
    private String email;

    /**
     * 头像
     */
    @Column(name = "avatar",columnDefinition = "varchar(1023) default ''")
    private String avatar;

    /**
     * 描述
     */
    @Column(name = "description",columnDefinition = "varchar(1023) default ''")
    private String description;

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
