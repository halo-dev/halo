package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.repository.CommentRepository;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * CommentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class CommentServiceImpl extends AbstractCrudService<Comment, Long> implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        super(commentRepository);
        this.commentRepository = commentRepository;
    }
}
