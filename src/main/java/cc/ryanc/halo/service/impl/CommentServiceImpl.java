package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.repository.CommentRepository;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;

/**
 * CommentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class CommentServiceImpl extends AbstractCrudService<Comment, Long> implements CommentService {

    private final CommentRepository commentRepository;

    private final PostService postService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostService postService) {
        super(commentRepository);
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    @Override
    public Page<CommentVO> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        // Build page request
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        // List all comments
        Page<Comment> comments = listAll(latestPageable);

        // Fetch goods ids
        Set<Integer> postIds = ServiceUtils.fetchProperty(comments.getContent(), Comment::getPostId);

        // Get all posts
        Map<Integer, Post> postMap = ServiceUtils.convertToMap(postService.listAllByIds(postIds), Post::getId);

        return comments.map(comment -> {
            // Convert to vo
            CommentVO commentVO = new CommentVO().convertFrom(comment);

            // Get post and set to the vo
            commentVO.setPost(new PostMinimalOutputDTO().convertFrom(postMap.get(comment.getPostId())));

            return commentVO;
        });
    }
}
