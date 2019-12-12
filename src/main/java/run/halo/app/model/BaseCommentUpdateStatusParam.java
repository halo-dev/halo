package run.halo.app.model;

import lombok.Data;
import run.halo.app.model.enums.CommentStatus;

import java.util.List;

/**
 * Base comment update status param.
 *
 * @author ryanwang
 * @date 2019-12-12
 */
@Data
public class BaseCommentUpdateStatusParam {

    private List<Long> ids;

    private CommentStatus status;
}
