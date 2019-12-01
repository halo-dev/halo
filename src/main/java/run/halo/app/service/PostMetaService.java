package run.halo.app.service;

import org.springframework.lang.NonNull;
import run.halo.app.model.dto.PostMetaDTO;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.service.base.BaseMetaService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post meta service interface.
 *
 * @author ryanwang
 * @author ikaisec
 * @author guqing
 * @date 2019-08-04
 */
public interface PostMetaService extends BaseMetaService<PostMeta> {

    /**
     * Creates by post metas and post id.
     *
     * @param postId    post id must not be null
     * @param postMetas post metas must not be null
     * @return a list of post meta
     */
    List<PostMeta> createOrUpdateByPostId(@NonNull Integer postId, @NonNull Set<PostMeta> postMetas);

    /**
     * Lists post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta
     */
    List<PostMeta> listPostMetasBy(@NonNull Integer postId);

    /**
     * Remove post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta
     */
    List<PostMeta> removeByPostId(@NonNull Integer postId);

    /**
     * Lists post metas as map.
     *
     * @param postIds post ids must not be null
     * @return a map of post meta
     */
    Map<Integer, List<PostMeta>> listPostMetaAsMap(@NonNull Set<Integer> postIds);

    /**
     * Convert PostMeta to PostMetaDTO.
     *
     * @param postMeta post meta must not be null
     * @return post meta vo
     */
    @NonNull
    PostMetaDTO convertTo(@NonNull PostMeta postMeta);

    /**
     * Convert list of PostMeta to list of PostMetaDTO.
     *
     * @param postMetaList post meta list must not be null
     * @return a list of post meta dto
     */
    @NonNull
    List<PostMetaDTO> convertTo(@NonNull List<PostMeta> postMetaList);
}
