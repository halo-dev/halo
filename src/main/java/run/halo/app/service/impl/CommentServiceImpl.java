package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.post.PostMinimalDTO;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.entity.Post;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.repository.CommentRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.CommentService;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CommentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class CommentServiceImpl extends BaseCommentServiceImpl<Comment> implements CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              OptionService optionService,
                              ApplicationEventPublisher eventPublisher) {
        super(commentRepository, optionService, eventPublisher);
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Page<CommentWithPostVO> convertToWithPostVo(Page<Comment> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");

        return new PageImpl<>(convertToWithPostVo(commentPage.getContent()), commentPage.getPageable(), commentPage.getTotalElements());

    }

    @Override
    public List<CommentWithPostVO> convertToWithPostVo(List<Comment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // Fetch goods ids
        Set<Integer> postIds = ServiceUtils.fetchProperty(comments, Comment::getPostId);

        // Get all posts
        Map<Integer, Post> postMap = ServiceUtils.convertToMap(postRepository.findAllById(postIds), Post::getId);

        return comments.stream().map(comment -> {
            // Convert to vo
            CommentWithPostVO commentWithPostVO = new CommentWithPostVO().convertFrom(comment);

            // Get post and set to the vo
            commentWithPostVO.setPost(new PostMinimalDTO().convertFrom(postMap.get(comment.getPostId())));

            return commentWithPostVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void targetMustExist(Integer postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("The post with id " + postId + " was not found");
        }
    }
}
