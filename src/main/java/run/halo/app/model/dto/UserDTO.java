package run.halo.app.model.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.MFAType;

/**
 * User output dto.
 *
 * @author johnniang
 * @date 3/16/19
 */
@Data
@ToString
@EqualsAndHashCode
public class UserDTO implements OutputConverter<UserDTO, User> {

    private Integer id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private String description;

    private MFAType mfaType;

    private Date createTime;

    private Date updateTime;
}
