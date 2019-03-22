package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.projection.CommentCountProjection;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

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

    /**
     * Finds all comments by post ids.
     *
     * @param postIds post ids must not be null
     * @return a list of comment
     */
    @NonNull
    List<Comment> findAllByPostIdIn(@NonNull Iterable<Integer> postIds);

    /**
     * Finds all comments by post id.
     *
     * @param postId post id must not be null
     * @return a list of comment
     */
    @NonNull
    List<Comment> findAllByPostId(@NonNull Integer postId);

    @Query("select new cc.ryanc.halo.model.projection.CommentCountProjection(count(comment.id), comment.postId) from Comment comment where comment.postId in ?1 group by comment.postId")
    @NonNull
    List<CommentCountProjection> countByPostIds(@NonNull Iterable<Integer> postIds);
}
