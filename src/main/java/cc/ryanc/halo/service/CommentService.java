package cc.ryanc.halo.service;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;

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
    Page<CommentVO> listLatest(int top);
}
