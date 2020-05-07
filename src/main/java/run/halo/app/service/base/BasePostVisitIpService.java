package run.halo.app.service.base;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseVisitIp;

import java.util.List;

/**
 * Base visit ip address service interface.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
public interface BasePostVisitIpService<VISITIP extends BaseVisitIp> extends CrudService<VISITIP, Long> {

    /**
     * Create by visitIp.
     *
     * @param visitIp visitIp must not be null
     * @return created visitIp
     */
    @NonNull
    VISITIP createBy(@NonNull VISITIP visitIp);

    /**
     * Gets all of the ip records of a post.
     *
     * @param postId post id must not ne null
     * @return a list of ip records
     */
    List<VISITIP> getIpRecordsByPostId(@NonNull Integer postId);

    /**
     * Check ip record's existence by post id and ip address.
     *
     * @param postId post id must not ne null
     * @param requestIp request ip must not be null
     * @return whether ip record exits (true: exit, false: not exist)
     */
    boolean checkIpRecordByPostIdAndIp(@NonNull Integer postId, @NonNull String requestIp);
}
