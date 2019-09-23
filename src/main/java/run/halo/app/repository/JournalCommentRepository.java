package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.projection.CommentChildrenCountProjection;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.repository.base.BaseCommentRepository;

import java.util.List;

/**
 * Journal comment repository.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface JournalCommentRepository extends BaseCommentRepository<JournalComment> {

    @Query("select new run.halo.app.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from JournalComment comment " +
            "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Iterable<Integer> postIds);

    @Query("select new run.halo.app.model.projection.CommentChildrenCountProjection(count(comment.id), comment.parentId) " +
            "from JournalComment comment " +
            "where comment.parentId in ?1 " +
            "group by comment.parentId")
    @NonNull
    @Override
    List<CommentChildrenCountProjection> findDirectChildrenCount(@NonNull Iterable<Long> commentIds);
}
