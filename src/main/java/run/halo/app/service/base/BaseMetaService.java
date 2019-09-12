package run.halo.app.service.base;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseMeta;
import run.halo.app.model.params.BaseMetaParam;

import java.util.List;
import java.util.Map;

/**
 * Base meta service interface.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
public interface BaseMetaService<META extends BaseMeta> extends CrudService<META, Long> {

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
}
