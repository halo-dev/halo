package run.halo.app.repository.base;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseVisitIp;

import java.util.List;

/**
 * Base visit ip address repository.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@NoRepositoryBean
public interface BaseVisitIpRepository<VISITIP extends BaseVisitIp> extends BaseRepository<VISITIP, Long> {

    /**
     * Finds all visitIp by post id.
     *
     * @param postId post id must not be null
     * @return a list of visitIp (ip address and visit time)
     */
    @NonNull
    List<VISITIP> findAllByPostId(@NonNull Integer postId);

    /**
     * Finds all visitIp by post id and request ip.
     *
     * @param postId post id must not be null
     * @param requestIp request ip must not be null
     * @return a list of visitIp (with 1 or 0 item)
     */
    List<VISITIP> findByPostIdAndIp(@NonNull Integer postId, @NonNull String requestIp);
}
