package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.utils.ReflectionUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.lang.reflect.ParameterizedType;

/**
 * Base Comment param.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Data
public abstract class BaseCommentParam<COMMENT> implements InputConverter<COMMENT> {

    @NotBlank(message = "PostComment author name must not be blank")
    @Size(max = 50, message = "Length of comment author name must not be more than {max}")
    private String author;

    @NotBlank(message = "PostComment email must not be blank")
    @Email(message = "PostComment email's format is incorrect")
    @Size(max = 255, message = "Length of comment email must not be more than {max}")
    private String email;

    @Size(max = 127, message = "Length of comment author url must not be more than {max}")
    private String authorUrl;

    @NotBlank(message = "PostComment content must not be blank")
    @Size(max = 1023, message = "Length of comment content must not be more than {max}")
    private String content;

    @Min(value = 1, message = "Post id must not be less than {value}")
    private Integer postId;

    @Min(value = 0, message = "PostComment parent id must not be less than {value}")
    private Long parentId = 0L;

    @Override
    public ParameterizedType parameterizedType() {
        return ReflectionUtils.getParameterizedTypeBySuperClass(BaseCommentParam.class, this.getClass());
    }
}
