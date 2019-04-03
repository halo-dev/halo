package run.halo.app.model.params;

import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Tag;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.SlugUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.SlugUtils;

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
