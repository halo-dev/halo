package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Attachment group repository
 *
 * @author guqing
 * @date 2020-10-24
 */
public interface AttachmentGroupRepository extends BaseRepository<AttachmentGroup, Integer>, JpaSpecificationExecutor<AttachmentGroup> {
    /**
     * List attachment groups by parent ids.
     *
     * @param groupIds a collection of group id
     * @return attachment groups of parent id in group ids
     */
    List<AttachmentGroup> findByParentIdIn(List<Integer> groupIds);

    /**
     * List attachment groups by parent id
     *
     * @param parentId groups parent id
     * @return attachment groups of parent equal to parentId
     */
    List<AttachmentGroup> findByParentId(Integer parentId);

    /**
     * Find one by name and parent id
     *
     * @param name attachment name
     * @param parentId attachment parent group id
     * @return a attachment group optional
     */
    Optional<AttachmentGroup> findByNameAndParentId(String name, Integer parentId);
}
