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

/**
 * @author johnniang
 * @date 19-4-24
 */
@Data
public class SheetParam implements InputConverter<Sheet> {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Length of title must not be more than {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

    @NotBlank(message = "Original content must not be blank")
    private String originalContent;

    @Size(max = 255, message = "Length of thumbnail must not be more than {max}")
    private String thumbnail;

    private Boolean disallowComment = false;

    @Size(max = 255, message = "Length of password must not be more than {max}")
    private String password;

    @Size(max = 255, message = "Length of template must not be more than {max}")
    private String template;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    @Override
    public Sheet convertTo() {
        if (StringUtils.isBlank(url)) {
            url = HaloUtils.normalizeUrl(title);
        } else {
            url = HaloUtils.normalizeUrl(url);
        }

        url = HaloUtils.initializeUrlIfBlank(url);

        Sheet sheet = InputConverter.super.convertTo();
        // Crypt password
        if (StringUtils.isNotBlank(password)) {
            sheet.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        return sheet;
    }

    @Override
    public void update(Sheet sheet) {
        if (StringUtils.isBlank(url)) {
            url = HaloUtils.normalizeUrl(title);
        } else {
            url = HaloUtils.normalizeUrl(url);
        }

        url = HaloUtils.initializeUrlIfBlank(url);

        InputConverter.super.update(sheet);

        // Crypt password
        if (StringUtils.isNotBlank(password)) {
            sheet.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
    }
}
