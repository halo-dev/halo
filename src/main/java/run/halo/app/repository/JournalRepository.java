package run.halo.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.repository.base.BaseCommentRepository;

import java.util.List;

/**
 * Journal repository.
 *
 * @author johnniang
 * @date 3/22/19
 */
public interface JournalRepository extends BaseCommentRepository<Journal> {

    /**
     * Counts comment count by post id collection.
     *
     * @param postIds post id collection must not be null
     * @return a list of comment count
     */
    @Query("select new run.halo.app.model.projection.CommentCountProjection(count(comment.id), comment.postId) from Journal comment where comment.postId in ?1 group by comment.postId")
    @NonNull
    List<CommentCountProjection> countByPostIds(@NonNull Iterable<Integer> postIds);

    /**
     * Finds all journals by parent id.
     *
     * @param parentId parent id must not be null
     * @param pageable page info must not be null
     * @return a page of journal
     */
    Page<Journal> findAllByParentId(@NonNull Long parentId, @NonNull Pageable pageable);
}
