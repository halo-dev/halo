package run.halo.app.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.router.ListRequest;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQuery extends ListRequest {
    // TODO add more query fields
}
