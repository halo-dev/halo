package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.entity.BaseMeta;
import run.halo.app.model.params.BaseMetaParam;
import run.halo.app.repository.base.BaseMetaRepository;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BaseMetaService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base meta service implementation.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@Slf4j
public abstract class BaseMetaServiceImpl<META extends BaseMeta> extends AbstractCrudService<META, Long> implements BaseMetaService<META> {

    private final BaseMetaRepository<META> baseMetaRepository;


    public BaseMetaServiceImpl(BaseMetaRepository<META> baseMetaRepository) {
        super(baseMetaRepository);
        this.baseMetaRepository = baseMetaRepository;
    }

    @Override
    public List<META> listBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseMetaRepository.findAllByPostId(postId);
    }

    @Override
    public META create(META meta) {
        Assert.notNull(meta, "Domain must not be null");

        // Check post id
        if (!ServiceUtils.isEmptyId(meta.getPostId())) {
            validateTarget(meta.getPostId());
        }

        // Create meta
        return super.create(meta);
    }

    @Override
    public META createBy(BaseMetaParam<META> metaParam) {
        Assert.notNull(metaParam, "Meta param must not be null");
        return create(metaParam.convertTo());
    }

    @Override
    public Map<String, Object> convertToMap(List<META> metas) {
        if (CollectionUtils.isEmpty(metas)) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        metas.forEach(meta -> result.put(meta.getKey(), meta.getValue()));

        return result;
    }
}
