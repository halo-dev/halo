package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <pre>
 *     附件持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/10
 */
public interface AttachmentRepository extends BaseRepository<Attachment, Long> {
}
