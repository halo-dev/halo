package run.halo.app.repository.base;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseMeta;

import java.util.List;
import java.util.Set;

/**
 * Base meta repository.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@NoRepositoryBean
public interface BaseMetaRepository<META extends BaseMeta> extends BaseRepository<META, Long>, JpaSpecificationExecutor<META> {

    /**
     * Finds all metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of meta
     */
    @NonNull
    List<META> findAllByPostId(@NonNull Integer postId);

    /**
     * Deletes post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta deleted
     */
    @NonNull
    List<META> deleteByPostId(@NonNull Integer postId);

    /**
     * Finds all post metas by post id.
     *
     * @param postIds post id must not be null
     * @return a list of post meta
     */
    @NonNull
    List<META> findAllByPostIdIn(@NonNull Set<Integer> postIds);
}
