package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Comment list with parent comment vo.
 *
 * @author johnniang
 * @date 3/31/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class CommentWithParentVO extends CommentOutputDTO implements Cloneable {

    /**
     * Parent comment.
     */
    private CommentWithParentVO parent;

    public CommentWithParentVO clone() {
        try {
            return (CommentWithParentVO) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Clone not support exception", e);
            return null;
        }
    }
}
