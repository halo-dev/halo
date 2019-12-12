package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.enums.PostStatus;

import java.util.List;

/**
 * Post status update param.
 *
 * @author ryanwang
 * @date 2019-12-12
 */
@Data
public class PostStatusUpdateParam {

    private PostStatus status;

    private List<Integer> ids;
}
