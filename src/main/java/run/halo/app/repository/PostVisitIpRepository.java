package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.PostVisitIp;
import run.halo.app.repository.base.BaseVisitIpRepository;

import java.util.List;

/**
 * Post visit ip address repository.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
public interface PostVisitIpRepository extends BaseVisitIpRepository<PostVisitIp>, JpaSpecificationExecutor<PostVisitIp> {

    /**
     * Finds all visitIp by post id.
     *
     * @param postId post id must not be null
     * @return a list of visitIp ordered by visit time
     */
    @NonNull
    @Query("select visit from PostVisitIp visit where visit.postId = :postId order by visit.createTime desc")
    List<PostVisitIp> findAllByPostId(@Param("postId") @NonNull Integer postId);

    /**
     * Finds all visitIp by post id and request ip.
     *
     * @param postId post id must not be null
     * @param requestIp request ip must not be null
     * @return a list of visitIp (with 1 or 0 item)
     */
    @Query("select visit from PostVisitIp visit where visit.postId = :postId and visit.ipAddress = :requestIp")
    List<PostVisitIp> findByPostIdAndIp(@Param("postId") @NonNull Integer postId, @Param("requestIp") @NonNull String requestIp);
}
