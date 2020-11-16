package run.halo.app.service.base;

import org.springframework.lang.NonNull;
import run.halo.app.model.dto.BaseMetaDTO;
import run.halo.app.model.entity.BaseMeta;
import run.halo.app.model.params.BaseMetaParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base meta service interface.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
public interface BaseMetaService<META extends BaseMeta> extends CrudService<META, Long> {

    /**
     * Creates by post metas and post id.
     *
     * @param postId post id must not be null
     * @param metas  metas must not be null
     * @return a list of post meta
     */
    List<META> createOrUpdateByPostId(@NonNull Integer postId, Set<META> metas);

    /**
     * Remove post metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of post meta
     */
    List<META> removeByPostId(@NonNull Integer postId);

    /**
     * Lists post metas as map.
     *
     * @param postIds post ids must not be null
     * @return a map of post meta
     */
    Map<Integer, List<META>> listPostMetaAsMap(@NonNull Set<Integer> postIds);

    /**
     * Lists metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of meta
     */
    @NonNull
    List<META> listBy(@NonNull Integer postId);

    /**
     * Creates a meta by meta.
     *
     * @param meta meta must not be null
     * @return created meta
     */
    @NonNull
    @Override
    META create(@NonNull META meta);

    /**
     * Creates a meta by meta param.
     *
     * @param metaParam meta param must not be null
     * @return created meta
     */
    @NonNull
    META createBy(@NonNull BaseMetaParam<META> metaParam);

    /**
     * Target validation.
     *
     * @param targetId target id must not be null (post id, sheet id)
     */
    void validateTarget(@NonNull Integer targetId);

    /**
     * Convert to map.
     *
     * @param metas a list of metas
     * @return a map of metas
     */
    Map<String, Object> convertToMap(List<META> metas);

    /**
     * Convert PostMeta to PostMetaDTO.
     *
     * @param postMeta post meta must not be null
     * @return post meta vo
     */
    @NonNull
    BaseMetaDTO convertTo(@NonNull META postMeta);

    /**
     * Convert list of PostMeta to list of PostMetaDTO.
     *
     * @param postMetaList post meta list must not be null
     * @return a list of post meta dto
     */
    @NonNull
    List<BaseMetaDTO> convertTo(@NonNull List<META> postMetaList);
}
