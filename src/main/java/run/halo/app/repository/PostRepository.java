package run.halo.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.base.BasePostRepository;

import java.util.Date;
import java.util.Optional;


/**
 * Post repository.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {

    @Query("select sum(p.visits) from Post p")
    Long countVisit();

    @Query("select sum(p.likes) from Post p")
    Long countLike();

    @NonNull
    Page<Post> findAllByStatusAndCreateTimeBefore(@NonNull PostStatus status, @NonNull Date createTime, @NonNull Pageable pageable);

    @NonNull
    Page<Post> findAllByStatusAndCreateTimeAfter(@NonNull PostStatus status, @NonNull Date createTime, @NonNull Pageable pageable);

    @NonNull
    Optional<Post> getByUrlAndStatus(@NonNull String url, @NonNull PostStatus status);

    @Modifying
    @Query("update Post p set p.visits = p.visits + :visits where p.id = :postId")
    int updateVisit(@Param("visits") long visits, @Param("postId") @NonNull Integer postId);

    @Modifying
    @Query("update Post p set p.likes = p.likes + :likes where p.id = :postId")
    int updateLikes(@Param("likes") long likes, @Param("postId") @NonNull Integer postId);
}
