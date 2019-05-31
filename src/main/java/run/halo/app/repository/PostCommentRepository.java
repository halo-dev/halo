package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.projection.CommentChildrenCountProjection;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.repository.base.BaseCommentRepository;

import java.util.List;

/**
 * PostComment repository.
 *
 * @author johnniang
 * @date 3/21/19
 */
public interface PostCommentRepository extends BaseCommentRepository<PostComment> {

    @Query("select new run.halo.app.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from PostComment comment " +
            "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Iterable<Integer> postIds);

    @Query("select new run.halo.app.model.projection.CommentChildrenCountProjection(count(comment.id), comment.parentId) " +
            "from PostComment comment " +
            "where comment.parentId in ?1 " +
            "group by comment.parentId")
    @NonNull
    List<CommentChildrenCountProjection> findDirectChildrenCount(@NonNull Iterable<Long> commentIds);
}
