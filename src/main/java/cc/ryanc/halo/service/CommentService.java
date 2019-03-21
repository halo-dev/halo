package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

/**
 * Comment service.
 *
 * @author johnniang
 */
public interface CommentService extends CrudService<Comment, Long> {

    /**
     * Lists latest comments.
     *
     * @param top top number must not be less than 0
     * @return a page of comments
     */
    @NonNull
    Page<CommentVO> pageLatest(int top);

    /**
     * Pages comments.
     *
     * @param status   comment status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<CommentVO> pageBy(@NonNull CommentStatus status, @NonNull Pageable pageable);
}
