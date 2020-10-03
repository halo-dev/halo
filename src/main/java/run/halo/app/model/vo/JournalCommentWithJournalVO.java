package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.dto.JournalDTO;

/**
 * Journal comment with journal vo.
 *
 * @author johnniang
 * @date 19-4-25
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JournalCommentWithJournalVO extends BaseCommentDTO {

    private JournalDTO journal;
}
