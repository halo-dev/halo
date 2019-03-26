package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.dto.base.InputConverter;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.SlugUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Tag param.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
public class TagParam implements InputConverter<Tag> {

    @NotBlank(message = "Tag name must not be blank")
    @Size(max = 255, message = "Length of tag name must not be more than {max}")
    private String name;

    @Size(max = 255, message = "Length of tag slug name must not be more than {max}")
    private String slugName;

    @Override
    public Tag convertTo() {
        if (StringUtils.isBlank(slugName)) {
            // Handle slug name
            slugName = SlugUtils.slugify(name);
        }

        slugName = HaloUtils.initializeUrlIfBlank(slugName);

        return InputConverter.super.convertTo();
    }
}
