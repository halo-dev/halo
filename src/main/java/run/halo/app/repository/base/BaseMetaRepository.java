package run.halo.app.repository.base;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseMeta;

/**
 * Base meta repository.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@NoRepositoryBean
public interface BaseMetaRepository<M extends BaseMeta>
    extends BaseRepository<M, Long>, JpaSpecificationExecutor<M> {

    /**
     * Finds all metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of meta
     */
    @NonNull
    List<M> findAllByPostId(@NonNull Integer postId);

    /**
     * Deletes post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta deleted
     */
    @NonNull
    List<M> deleteByPostId(@NonNull Integer postId);

    /**
     * Finds all post metas by post id.
     *
     * @param postIds post id must not be null
     * @return a list of post meta
     */
    @NonNull
    List<M> findAllByPostIdIn(@NonNull Set<Integer> postIds);
}
