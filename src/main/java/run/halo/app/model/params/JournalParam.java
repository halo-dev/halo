package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;

import javax.validation.constraints.NotBlank;

/**
 * Journal param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-4-25
 */
@Data
public class JournalParam implements InputConverter<Journal> {

    @NotBlank(message = "内容不能为空")
    private String sourceContent;

    private JournalType type = JournalType.PUBLIC;
}
