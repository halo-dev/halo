package run.halo.app.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.projection.CommentChildrenCountProjection;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.repository.base.BaseCommentRepository;

/**
 * PostComment repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-21
 */
public interface PostCommentRepository extends BaseCommentRepository<PostComment> {

    /**
     * Count comments by post ids.
     *
     * @param postIds post id collection must not be null
     * @return a list of CommentCountProjection
     */
    @Query(
        "select new run.halo.app.model.projection.CommentCountProjection(count(comment.id), "
            + "comment.postId) "
            + "from PostComment comment "
            + "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> postIds);

    /**
     * Finds direct children count by comment ids.
     *
     * @param commentIds comment ids must not be null.
     * @return a list of CommentChildrenCountProjection
     */
    @Query(
        "select new run.halo.app.model.projection.CommentChildrenCountProjection(count(comment"
            + ".id), comment.parentId) "
            + "from PostComment comment "
            + "where comment.parentId in ?1 "
            + "group by comment.parentId")
    @NonNull
    @Override
    List<CommentChildrenCountProjection> findDirectChildrenCount(
        @NonNull Collection<Long> commentIds);

    /**
     * 根据时间范围和IP地址统计评论次数
     *
     * @param ipAddress IP地址
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return 评论次数
     */
    @Query("SELECT COUNT(id) FROM PostComment WHERE ipAddress=?1 AND updateTime BETWEEN ?2 AND ?3"
        + " AND status <> 2")
    int countByIpAndTime(String ipAddress, Date startTime, Date endTime);
}
