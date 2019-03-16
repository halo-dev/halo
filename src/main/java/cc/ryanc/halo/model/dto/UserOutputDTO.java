package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * User output dto.
 *
 * @author johnniang
 * @date 3/16/19
 */
@Data
@ToString
@EqualsAndHashCode
public class UserOutputDTO implements OutputConverter<UserOutputDTO, User> {

    /**
     * User id.
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 描述
     */
    private String description;

    /**
     * Expire time.
     */
    private Date expireTime;

    /**
     * 创建时间戳
     */
    private Date createTime;

    /**
     * 更新时间戳
     */
    private Date updateTime;
}
