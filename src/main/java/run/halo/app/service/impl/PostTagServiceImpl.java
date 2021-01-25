package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.TagWithPostCountDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostTag;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.TagPostPostCountProjection;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.PostTagRepository;
import run.halo.app.repository.TagRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostTagService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

/**
 * Post tag service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer>
    implements PostTagService {

    private final PostTagRepository postTagRepository;

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final OptionService optionService;

    public PostTagServiceImpl(PostTagRepository postTagRepository,
        PostRepository postRepository,
        TagRepository tagRepository,
        OptionService optionService) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.optionService = optionService;
    }

    @Override
    public List<Tag> listTagsBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all tag ids
        Set<Integer> tagIds = postTagRepository.findAllTagIdsByPostId(postId);

        return tagRepository.findAllById(tagIds);
    }

    @Override
    public List<TagWithPostCountDTO> listTagWithCountDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // Find all tags
        List<Tag> tags = tagRepository.findAll(sort);

        // Find all post count
        Map<Integer, Long> tagPostCountMap = ServiceUtils
            .convertToMap(postTagRepository.findPostCount(), TagPostPostCountProjection::getTagId,
                TagPostPostCountProjection::getPostCount);

        // Find post count
        return tags.stream().map(
            tag -> {
                TagWithPostCountDTO tagWithCountOutputDTO =
                    new TagWithPostCountDTO().convertFrom(tag);
                tagWithCountOutputDTO.setPostCount(tagPostCountMap.getOrDefault(tag.getId(), 0L));

                StringBuilder fullPath = new StringBuilder();

                if (optionService.isEnabledAbsolutePath()) {
                    fullPath.append(optionService.getBlogBaseUrl());
                }

                fullPath.append(URL_SEPARATOR)
                    .append(optionService.getTagsPrefix())
                    .append(URL_SEPARATOR)
                    .append(tag.getSlug())
                    .append(optionService.getPathSuffix());

                tagWithCountOutputDTO.setFullPath(fullPath.toString());

                return tagWithCountOutputDTO;
            }
        ).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Tag>> listTagListMapBy(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all post tags
        List<PostTag> postTags = postTagRepository.findAllByPostIdIn(postIds);

        // Fetch tag ids
        Set<Integer> tagIds = ServiceUtils.fetchProperty(postTags, PostTag::getTagId);

        // Find all tags
        List<Tag> tags = tagRepository.findAllById(tagIds);

        // Convert to tag map
        Map<Integer, Tag> tagMap = ServiceUtils.convertToMap(tags, Tag::getId);

        // Create tag list map
        Map<Integer, List<Tag>> tagListMap = new HashMap<>();

        // Foreach and collect
        postTags.forEach(
            postTag -> tagListMap.computeIfAbsent(postTag.getPostId(), postId -> new LinkedList<>())
                .add(tagMap.get(postTag.getTagId())));

        return tagListMap;
    }


    @Override
    public List<Post> listPostsBy(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(Integer tagId, PostStatus status) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(status, "Post status must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId, status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(String slug, PostStatus status) {
        Assert.notNull(slug, "Tag slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Tag tag = tagRepository.getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该标签的信息").setErrorData(slug));

        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tag.getId(), status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public Page<Post> pagePostsBy(Integer tagId, Pageable pageable) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public Page<Post> pagePostsBy(Integer tagId, PostStatus status, Pageable pageable) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public List<PostTag> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> tagIds) {
        Assert.notNull(postId, "Post id must not be null");

        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        // Create post tags
        List<PostTag> postTagsStaging = tagIds.stream().map(tagId -> {
            // Build post tag
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            return postTag;
        }).collect(Collectors.toList());

        List<PostTag> postTagsToRemove = new LinkedList<>();
        List<PostTag> postTagsToCreate = new LinkedList<>();

        List<PostTag> postTags = postTagRepository.findAllByPostId(postId);

        postTags.forEach(postTag -> {
            if (!postTagsStaging.contains(postTag)) {
                postTagsToRemove.add(postTag);
            }
        });

        postTagsStaging.forEach(postTagStaging -> {
            if (!postTags.contains(postTagStaging)) {
                postTagsToCreate.add(postTagStaging);
            }
        });

        // Remove post tags
        removeAll(postTagsToRemove);

        // Remove all post tags need to remove
        postTags.removeAll(postTagsToRemove);

        // Add all created post tags
        postTags.addAll(createInBatch(postTagsToCreate));

        // Return post tags
        return postTags;
    }

    @Override
    public List<PostTag> listByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostTag> listByTagId(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        return postTagRepository.findAllByTagId(tagId);
    }

    @Override
    public Set<Integer> listTagIdsByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.findAllTagIdsByPostId(postId);
    }

    @Override
    public List<PostTag> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostTag> removeByTagId(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        return postTagRepository.deleteByTagId(tagId);
    }
}
