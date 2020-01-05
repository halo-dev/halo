package run.halo.app.model.params;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Journal query params.
 *
 * @author ryanwang
 * @date 2019/05/07
 */
@Data
public class MailParam {

    @NotBlank(message = "收件人不能为空")
    @Email(message = "邮箱格式错误")
    private String to;

    @NotBlank(message = "主题不能为空")
    private String subject;

    @NotBlank(message = "内容不能为空")
    private String content;
}
