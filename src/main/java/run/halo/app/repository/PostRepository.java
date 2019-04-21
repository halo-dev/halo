package run.halo.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
}
