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

    /**
     * Finds the latest version by post id and post status.
     *
     * @param postId post id
     * @param status record status to query
     * @return a {@link ContentPatchLog} record
     */
    ContentPatchLog findFirstByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);

    /**
     * Finds the latest version by post id.
     *
     * @param postId post id to query
     * @return a {@link ContentPatchLog} record of the latest version queried bby post id
     */
    ContentPatchLog findFirstByPostIdOrderByVersionDesc(Integer postId);

    /**
     * Finds all records below the specified version number by post id and status.
     *
     * @param postId post id
     * @param version version number
     * @param status record status
     * @return records below the specified version
     */
    @Query("from ContentPatchLog c where c.postId = :postId and c.version <= :version and c"
        + ".status=:status order by c.version desc")
    List<ContentPatchLog> findByPostIdAndStatusAndVersionLessThan(Integer postId, Integer version,
        PostStatus status);

    /**
     * Finds by post id and version
     *
     * @param postId post id
     * @param version version number
     * @return a {@link ContentPatchLog} record queried by post id and version
     */
    ContentPatchLog findByPostIdAndVersion(Integer postId, Integer version);

    /**
     * Finds all records by post id and status and based on version number descending order
     *
     * @param postId post id
     * @param status status
     * @return a list of {@link ContentPatchLog} queried by post id and status
     */
    List<ContentPatchLog> findAllByPostIdAndStatusOrderByVersionDesc(Integer postId,
        PostStatus status);

    /**
     * Finds all records by post id.
     *
     * @param postId post id to query
     * @return a list of {@link ContentPatchLog} queried by post id
     */
    List<ContentPatchLog> findAllByPostId(Integer postId);
}
