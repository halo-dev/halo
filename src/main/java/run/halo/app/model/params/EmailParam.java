package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Email;
import run.halo.app.model.support.CreateCheck;
import run.halo.app.model.support.UpdateCheck;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Email param.
 *
 * @author johnniang
 * @date 4/3/19
 */
@Data
public class EmailParam implements InputConverter<Email> {

    @NotBlank(message = "邮箱名称不能为空")
    @Size(max = 255, message = "邮箱名称的字符长度不能超过 {max}")
    private String name;

    @javax.validation.constraints.Email(message = "电子邮件地址的格式不正确", groups = {CreateCheck.class, UpdateCheck.class})
    @NotBlank(message = "电子邮件地址不能为空", groups = {CreateCheck.class, UpdateCheck.class})
    @Size(max = 127, message = "电子邮件的字符长度不能超过 {max}", groups = {CreateCheck.class, UpdateCheck.class})
    private String value;

    @Size(max = 255, message = "邮箱描述的字符长度不能超过 {max}")
    private String description;
}
