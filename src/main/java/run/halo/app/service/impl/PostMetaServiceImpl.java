package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.PostMetaDTO;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.repository.PostMetaRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.PostMetaService;
import run.halo.app.utils.ServiceUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Post meta service implementation class.
 *
 * @author ryanwang
 * @author ikaisec
 * @author guqing
 * @date 2019-08-04
 */
@Slf4j
@Service
public class PostMetaServiceImpl extends BaseMetaServiceImpl<PostMeta> implements PostMetaService {

    private final PostMetaRepository postMetaRepository;

    private final PostRepository postRepository;

    public PostMetaServiceImpl(PostMetaRepository postMetaRepository,
                               PostRepository postRepository) {
        super(postMetaRepository);
        this.postMetaRepository = postMetaRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void validateTarget(Integer postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(postId));
    }

    @Override
    public List<PostMeta> createOrUpdateByPostId(Integer postId, Set<PostMeta> postMetas) {
        Assert.notNull(postId, "Post id must not be null");
        if (CollectionUtils.isEmpty(postMetas)) {
            return Collections.emptyList();
        }

        // firstly remove post metas by post id
        removeByPostId(postId);

        // Save post metas
        postMetas.forEach(postMeta -> {
            postMeta.setPostId(postId);
            postMetaRepository.save(postMeta);
        });
        return new ArrayList<>(postMetas);
    }

    @Override
    public List<PostMeta> listPostMetasBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return postMetaRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostMeta> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null of removeByPostId");
        return postMetaRepository.deleteByPostId(postId);
    }

    @Override
    public Map<Integer, List<PostMeta>> listPostMetaAsMap(Set<Integer> postIds) {
        Assert.notNull(postIds, "Post ids must not be null");
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all post metas
        List<PostMeta> postMetas = postMetaRepository.findAllByPostIdIn(postIds);

        // Convert to post meta map
        Map<Long, PostMeta> postMetaMap = ServiceUtils.convertToMap(postMetas, PostMeta::getId);

        // Create category list map
        Map<Integer, List<PostMeta>> postMetaListMap = new HashMap<>();

        // Foreach and collect
        postMetas.forEach(postMeta -> postMetaListMap.computeIfAbsent(postMeta.getPostId(), postId -> new LinkedList<>())
                .add(postMetaMap.get(postMeta.getId())));

        return postMetaListMap;
    }

    @Override
    public PostMetaDTO convertTo(PostMeta postMeta) {
        Assert.notNull(postMeta, "Category must not be null");

        return new PostMetaDTO().convertFrom(postMeta);
    }

    @Override
    public List<PostMetaDTO> convertTo(List<PostMeta> postMetaList) {
        if (CollectionUtils.isEmpty(postMetaList)) {
            return Collections.emptyList();
        }

        return postMetaList.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }
}
