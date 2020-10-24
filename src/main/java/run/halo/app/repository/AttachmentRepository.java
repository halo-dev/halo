package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

/**
 * Attachment repository
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-04-03
 */
public interface AttachmentRepository extends BaseRepository<Attachment, Integer>, JpaSpecificationExecutor<Attachment> {

    /**
     * Find all attachment media type.
     *
     * @return list of media type.
     */
    @Query(value = "select distinct a.mediaType from Attachment a")
    List<String> findAllMediaType();

    /**
     * Find all attachment type.
     *
     * @return list of type.
     */
    @Query(value = "select distinct a.type from Attachment a")
    List<AttachmentType> findAllType();

    /**
     * Counts by attachment path.
     *
     * @param path attachment path must not be blank
     * @return count of the given path
     */
    long countByPath(@NonNull String path);

    /**
     * Find all attachments that group id in groupIds
     * @param groupIds attachment group ids
     * @return attachment id that group id in groupIds
     */
    List<Integer> findByGroupIdIn(List<Integer> groupIds);

    /**
     * Find all attachments that group id is null
     * @return a list of attachment
     */
    List<Attachment> findByGroupIdIsNull();

    /**
     * Find all attachments that group id equals groupId
     * @param groupId attachment group id
     * @return a list of attachment
     */
    List<Attachment> findByGroupId(Integer groupId);
}
