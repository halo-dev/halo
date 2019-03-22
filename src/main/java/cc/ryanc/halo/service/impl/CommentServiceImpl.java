package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.projection.CommentCountProjection;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.repository.CommentRepository;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CommentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class CommentServiceImpl extends AbstractCrudService<Comment, Long> implements CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository) {
        super(commentRepository);
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Page<CommentVO> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        // Build page request
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        return convertBy(listAll(latestPageable));
    }

    @Override
    public Page<CommentVO> pageBy(CommentStatus status, Pageable pageable) {
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all 
        Page<Comment> commentPage = commentRepository.findAllByStatus(status, pageable);

        return convertBy(commentPage);
    }

    @Override
    public List<Comment> listBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return commentRepository.findAllByPostId(postId);
    }

    @Override
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Get all comment counts
        List<CommentCountProjection> commentCountProjections = commentRepository.countByPostIds(postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId, CommentCountProjection::getCount);
    }

    /**
     * Converts to comment vo page.
     *
     * @param commentPage comment page must not be null
     * @return a page of comment vo
     */
    @NonNull
    private Page<CommentVO> convertBy(@NonNull Page<Comment> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");

        return new PageImpl<>(convertBy(commentPage.getContent()), commentPage.getPageable(), commentPage.getTotalElements());
    }

    /**
     * Converts to comment vo list.
     *
     * @param comments comment list
     * @return a list of comment vo
     */
    @NonNull
    private List<CommentVO> convertBy(@Nullable List<Comment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // Fetch goods ids
        Set<Integer> postIds = ServiceUtils.fetchProperty(comments, Comment::getPostId);

        // Get all posts
        Map<Integer, Post> postMap = ServiceUtils.convertToMap(postRepository.findAllById(postIds), Post::getId);

        return comments.stream().map(comment -> {
            // Convert to vo
            CommentVO commentVO = new CommentVO().convertFrom(comment);

            // Get post and set to the vo
            commentVO.setPost(new PostMinimalOutputDTO().convertFrom(postMap.get(comment.getPostId())));

            return commentVO;
        }).collect(Collectors.toList());
    }
}
