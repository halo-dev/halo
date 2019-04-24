package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.repository.base.BaseCommentRepository;

import java.util.List;

/**
 * Comment repository.
 *
 * @author johnniang
 * @date 3/21/19
 */
public interface CommentRepository extends BaseCommentRepository<Comment> {

    @Query("select new run.halo.app.model.projection.CommentCountProjection(count(comment.id), comment.postId) from BaseComment comment where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Iterable<Integer> postIds);

}
