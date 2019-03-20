package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.TagWithCountOutputDTO;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostTag;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.repository.PostTagRepository;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.PostTagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Post tag service implementation.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer> implements PostTagService {

    private final PostTagRepository postTagRepository;

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    public PostTagServiceImpl(PostTagRepository postTagRepository,
                              PostRepository postRepository,
                              TagRepository tagRepository) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> listTagsBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all tag ids
        Set<Integer> tagIds = postTagRepository.findAllTagIdsByPostId(postId);

        return tagRepository.findAllById(tagIds);
    }

    @Override
    public List<TagWithCountOutputDTO> listTagWithCountDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // Find all tags
        List<Tag> tags = tagRepository.findAll(sort);

        // Find post count
        return tags.stream().map(tag -> {
            TagWithCountOutputDTO tagOutputDTO = new TagWithCountOutputDTO().convertFrom(tag);

            return tagOutputDTO;
        }).collect(Collectors.toList());
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
        postTags.forEach(postTag -> tagListMap.computeIfAbsent(postTag.getPostId(), postId -> new LinkedList<>()).add(tagMap.get(postTag.getTagId())));

        return tagListMap;
    }


    @Override
    public List<Post> listPostsBy(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId);

        return postRepository.findAllById(postIds);
    }
}
