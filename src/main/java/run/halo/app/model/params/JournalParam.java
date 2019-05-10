package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Journal;

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

    @NotBlank(message = "Content must not be blank")
    @Size(max = 511, message = "Length of comment content must not be more than {max}")
    private String content;
}
