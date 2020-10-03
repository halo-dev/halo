package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.BaseMetaDTO;
import run.halo.app.model.entity.BaseMeta;
import run.halo.app.model.params.BaseMetaParam;
import run.halo.app.repository.base.BaseMetaRepository;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BaseMetaService;
import run.halo.app.utils.ServiceUtils;

import java.util.*;
import java.util.stream.Collectors;

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
    @Transactional
    public List<META> createOrUpdateByPostId(@NotNull Integer postId, Set<META> metas) {
        Assert.notNull(postId, "Post id must not be null");

        // firstly remove post metas by post id
        removeByPostId(postId);

        if (CollectionUtils.isEmpty(metas)) {
            return Collections.emptyList();
        }

        // Save post metas
        metas.forEach(postMeta -> {
            if (StringUtils.isNotEmpty(postMeta.getValue()) && StringUtils.isNotEmpty(postMeta.getKey())) {
                postMeta.setPostId(postId);
                baseMetaRepository.save(postMeta);
            }
        });
        return new ArrayList<>(metas);
    }

    @Override
    public List<META> removeByPostId(@NotNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null of removeByPostId");
        return baseMetaRepository.deleteByPostId(postId);
    }

    @Override
    public Map<Integer, List<META>> listPostMetaAsMap(@NotNull Set<Integer> postIds) {
        Assert.notNull(postIds, "Post ids must not be null");
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all metas
        List<META> metas = baseMetaRepository.findAllByPostIdIn(postIds);

        // Convert to meta map
        Map<Long, META> postMetaMap = ServiceUtils.convertToMap(metas, META::getId);

        // Create category list map
        Map<Integer, List<META>> postMetaListMap = new HashMap<>();

        // Foreach and collect
        metas.forEach(meta -> postMetaListMap.computeIfAbsent(meta.getPostId(), postId -> new LinkedList<>())
                .add(postMetaMap.get(meta.getId())));

        return postMetaListMap;
    }

    @Override
    public @NotNull List<META> listBy(@NotNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseMetaRepository.findAllByPostId(postId);
    }

    @Override
    public @NotNull META create(@NotNull META meta) {
        Assert.notNull(meta, "Domain must not be null");

        // Check post id
        if (!ServiceUtils.isEmptyId(meta.getPostId())) {
            validateTarget(meta.getPostId());
        }

        // Create meta
        return super.create(meta);
    }

    @Override
    public @NotNull META createBy(@NotNull BaseMetaParam<META> metaParam) {
        Assert.notNull(metaParam, "Meta param must not be null");
        return create(metaParam.convertTo());
    }

    @Override
    public Map<String, Object> convertToMap(List<META> metas) {
        return ServiceUtils.convertToMap(metas, META::getKey, META::getValue);
    }


    @Override
    public @NotNull BaseMetaDTO convertTo(@NotNull META postMeta) {
        Assert.notNull(postMeta, "Category must not be null");

        return new BaseMetaDTO().convertFrom(postMeta);
    }

    @Override
    public @NotNull List<BaseMetaDTO> convertTo(@NotNull List<META> postMetaList) {
        if (CollectionUtils.isEmpty(postMetaList)) {
            return Collections.emptyList();
        }

        return postMetaList.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }
}
