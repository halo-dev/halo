package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Comment repository.
 *
 * @author johnniang
 * @date 3/21/19
 */
public interface CommentRepository extends BaseRepository<Comment, Long> {

    /**
     * Finds all comments by status.
     *
     * @param status   status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<Comment> findAllByStatus(@Nullable CommentStatus status, @NonNull Pageable pageable);
}
