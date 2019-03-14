package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

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
}
