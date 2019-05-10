package run.halo.app.model.params;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.entity.JournalComment;

/**
 * Journal comment param.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JournalCommentParam extends BaseCommentParam<JournalComment> {

}
