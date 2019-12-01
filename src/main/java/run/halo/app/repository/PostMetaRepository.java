package run.halo.app.repository;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.repository.base.BaseMetaRepository;

import java.util.List;
import java.util.Set;

/**
 * PostMeta repository.
 *
 * @author ryanwang
 * @author ikaisec
 * @author guqing
 * @date 2019-08-04
 */
public interface PostMetaRepository extends BaseMetaRepository<PostMeta> {

    /**
     * Deletes post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta deleted
     */
    @NonNull
    List<PostMeta> deleteByPostId(@NonNull Integer postId);

    /**
     * Finds all post metas by post id.
     * @param postIds post id must not be null
     * @return a list of post meta
     */
    @NonNull
    List<PostMeta> findAllByPostIdIn(@NonNull Set<Integer> postIds);
}
