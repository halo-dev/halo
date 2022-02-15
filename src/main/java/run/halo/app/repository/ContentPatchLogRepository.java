package run.halo.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.base.BaseRepository;

/**
 * Content patch log repository.
 *
 * @author guqing
 * @since 2022-01-04
 */
public interface ContentPatchLogRepository extends BaseRepository<ContentPatchLog, Integer> {

    ContentPatchLog findFirstByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);

    ContentPatchLog findFirstByPostIdOrderByVersionDesc(Integer postId);

    @Query("from ContentPatchLog c where c.postId = :postId and c.version <= :version and c"
        + ".status=:status order by c.version desc")
    List<ContentPatchLog> findByPostIdAndStatusAndVersionLessThan(Integer postId, Integer version,
        PostStatus status);

    ContentPatchLog findByPostIdAndVersion(Integer postId, Integer version);

    List<ContentPatchLog> findAllByPostIdAndStatusOrderByVersionDesc(Integer postId,
        PostStatus status);
}
