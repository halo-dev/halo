package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Post service implementation.
 *
 * @author johnniang
 */
@Service
public class PostServiceImpl extends AbstractCrudService<Post, Integer> implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        super(postRepository);
        this.postRepository = postRepository;
    }

    @Override
    public Page<PostSimpleOutputDTO> listLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "editTime"));

        Page<Post> posts = listAll(latestPageable);

        return posts.map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }
}
