package run.halo.app.model.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 两步验证参数
 *
 * @author xun404
 * @date 2020-3-26
 */
@Data
public class TwoFactorAuthParam {

    private boolean isOpen = false;

    private String tfaKey;

    @NotBlank(message = "两步验证Code不能为空")
    @Size(min = 6, max = 6, message = "两步验证Code应为 {max} 位")
    private String tfaCode;

}
