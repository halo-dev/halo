package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.dto.base.InputConverter;
import cc.ryanc.halo.model.entity.Comment;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Comment param.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Data
public class CommentParam implements InputConverter<Comment> {

    @NotBlank(message = "Comment author name must not be blank")
    @Size(max = 50, message = "Length of comment author name must not be more than {max}")
    private String author;

    @NotBlank(message = "Comment email must not be blank")
    @Email(message = "Comment email's format is incorrect")
    @Size(max = 255, message = "Length of comment email must not be more than {max}")
    private String email;

    @Size(max = 127, message = "Length of comment author url must not be more than {max}")
    private String authorUrl;

    @NotBlank(message = "Comment content must not be blank")
    @Size(max = 1023, message = "Length of comment content must not be more than {max}")
    private String content;

    @Min(value = 1, message = "Post id must not be less than {value}")
    private Integer postId;

    @Min(value = 0, message = "Comment parent id must not be less than {value}")
    private Long parentId = 0L;

}
