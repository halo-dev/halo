package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Tag;
import run.halo.app.utils.SlugUtils;

/**
 * Tag param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-20
 */
@Data
public class TagParam implements InputConverter<Tag> {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 255, message = "标签名称的字符长度不能超过 {max}")
    private String name;

    @Size(max = 255, message = "标签别名的字符长度不能超过 {max}")
    private String slug;

    @Size(max = 1023, message = "封面图链接的字符长度不能超过 {max}")
    private String thumbnail;

    @Override
    public Tag convertTo() {

        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(name) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        return InputConverter.super.convertTo();
    }

    @Override
    public void update(Tag tag) {

        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(name) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        InputConverter.super.update(tag);
    }
}
