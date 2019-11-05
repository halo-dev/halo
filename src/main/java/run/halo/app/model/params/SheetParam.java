package run.halo.app.model.params;

import cn.hutool.crypto.digest.BCrypt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.HaloUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Sheet param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 19-4-24
 */
@Data
public class SheetParam implements InputConverter<Sheet> {

    @NotBlank(message = "页面标题不能为空")
    @Size(max = 100, message = "页面标题的字符长度不能超过 {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

    @NotBlank(message = "页面内容不能为空")
    private String originalContent;

    @Size(max = 255, message = "页面缩略图链接的字符长度不能超过 {max}")
    private String thumbnail;

    private Boolean disallowComment = false;

    private Date createTime;

    @Size(max = 255, message = "Length of password must not be more than {max}")
    private String password;

    @Size(max = 255, message = "Length of template must not be more than {max}")
    private String template;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    @Override
    public Sheet convertTo() {
        if (StringUtils.isBlank(url)) {
            url = title.replace(".","");
        }

        if (null == thumbnail) {
            thumbnail = "";
        }

        return InputConverter.super.convertTo();
    }

    @Override
    public void update(Sheet sheet) {
        if (StringUtils.isBlank(url)) {
            url = title.replace(".","");
        }

        if (null == thumbnail) {
            thumbnail = "";
        }

        InputConverter.super.update(sheet);
    }
}
