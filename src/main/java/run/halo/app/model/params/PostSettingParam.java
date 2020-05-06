package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Post;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/**
 * Post param.
 *
 * @author Holldean
 * @date 2020-05-05
 */
@Data
public class PostSettingParam implements InputConverter<Post> {

    private List<Integer> ids;

    private Boolean disallowComment = false;

    @Size(max = 255, message = "文章密码的字符长度不能超过 {max}")
    private String password;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;

}
