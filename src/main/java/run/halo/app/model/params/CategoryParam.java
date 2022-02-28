package run.halo.app.model.params;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Category;
import run.halo.app.model.support.NotAllowSpaceOnly;
import run.halo.app.utils.SlugUtils;

/**
 * Category param.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-21
 */
@Data
public class CategoryParam implements InputConverter<Category> {

    private Integer id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 255, message = "分类名称的字符长度不能超过 {max}")
    private String name;

    @Size(max = 255, message = "分类别名的字符长度不能超过 {max}")
    private String slug;

    @Size(max = 100, message = "分类描述的字符长度不能超过 {max}")
    private String description;

    @Size(max = 1023, message = "封面图链接的字符长度不能超过 {max}")
    private String thumbnail;

    @Size(max = 255, message = "分类密码的字符长度不能超过 {max}")
    @NotAllowSpaceOnly(message = "密码开头和结尾不能包含空字符串")
    private String password;

    private Integer parentId = 0;

    @Min(value = 0, message = "排序编号不能低于 {value}")
    private Integer priority;

    @Override
    public Category convertTo() {

        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(name) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        return InputConverter.super.convertTo();
    }

    @Override
    public void update(Category category) {

        slug = StringUtils.isBlank(slug) ? SlugUtils.slug(name) : SlugUtils.slug(slug);

        if (null == thumbnail) {
            thumbnail = "";
        }

        InputConverter.super.update(category);
    }
}
