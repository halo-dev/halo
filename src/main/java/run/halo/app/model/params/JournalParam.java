package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Journal;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Journal param.
 *
 * @author johnniang
 * @date 19-4-25
 */
@Data
public class JournalParam implements InputConverter<Journal> {

    @NotBlank(message = "Author name must not be blank")
    @Size(max = 50, message = "Length of comment author name must not be more than {max}")
    private String author;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email's format is incorrect")
    @Size(max = 255, message = "Length of comment email must not be more than {max}")
    private String email;

    @Size(max = 127, message = "Length of comment author url must not be more than {max}")
    private String authorUrl;

    @NotBlank(message = "Content must not be blank")
    @Size(max = 511, message = "Length of comment content must not be more than {max}")
    private String content;

    @Min(value = 0, message = "Parent id must not be less than {value}")
    private Long parentId = 0L;
}
