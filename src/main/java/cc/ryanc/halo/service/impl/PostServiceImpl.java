package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.model.vo.PostListVO;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostCategoryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.PostTagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Post service implementation.
 *
 * @author johnniang
 * @author RYAN0UP
 */
@Service
public class PostServiceImpl extends AbstractCrudService<Post, Integer> implements PostService {

    private final PostRepository postRepository;

    private final PostTagService postTagService;

    private final PostCategoryService postCategoryService;

    public PostServiceImpl(PostRepository postRepository,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService) {
        super(postRepository);
        this.postRepository = postRepository;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
    }

    /**
     * Save post with tags and categories
     *
     * @param post       post
     * @param tags       tags
     * @param categories categories
     * @return saved post
     */
    @Override
    public Post save(Post post, List<Tag> tags, List<Category> categories) {
        // TODO 保存文章以及对应标签和分类
        return null;
    }

    /**
     * Remove post and relationship
     *
     * @param id id
     */
    @Override
    public void remove(Integer id) {
        // TODO 删除文章以及关联关系
    }

    @Override
    public Page<PostMinimalOutputDTO> pageLatestOfMinimal(int top) {
        return pageLatest(top).map(post -> new PostMinimalOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostSimpleOutputDTO> pageLatestOfSimple(int top) {
        return pageLatest(top).map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<Post> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "editTime"));

        return listAll(latestPageable);
    }

    @Override
    public Page<Post> pageBy(PostStatus status, PostType type, Pageable pageable) {
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(type, "Post type must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        return postRepository.findAllByStatusAndType(status, type, pageable);
    }

    /**
     * List by status and type
     *
     * @param status   status
     * @param type     type
     * @param pageable pageable
     * @return Page<PostSimpleOutputDTO>
     */
    @Override
    public Page<PostSimpleOutputDTO> pageSimpleDtoByStatus(PostStatus status, PostType type, Pageable pageable) {
        return pageBy(status, type, pageable).map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostListVO> pageListVoBy(PostStatus status, PostType type, Pageable pageable) {
        Page<Post> postPage = pageBy(status, type, pageable);

        List<Post> posts = postPage.getContent();

        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        return postPage.map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            // Set tags
            List<TagOutputDTO> tagOutputDTOS = tagListMap.get(post.getId()).stream().map(tag -> (TagOutputDTO) new TagOutputDTO().convertFrom(tag)).collect(Collectors.toList());
            postListVO.setTags(tagOutputDTOS);

            // Set categories
            List<CategoryOutputDTO> categoryOutputDTOS = categoryListMap.get(post.getId()).stream().map(category -> (CategoryOutputDTO) new CategoryOutputDTO().convertFrom(category)).collect(Collectors.toList());
            postListVO.setCategories(categoryOutputDTOS);

            return postListVO;
        });
    }

    /**
     * Count posts by status and type
     *
     * @param status status
     * @param type   type
     * @return posts count
     */
    @Override
    public Long countByStatus(PostStatus status, PostType type) {
        return postRepository.countByStatusAndType(status, type);
    }
}
