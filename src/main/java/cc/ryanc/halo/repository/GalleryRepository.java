package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <pre>
 *     图库持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/2/26
 */
public interface GalleryRepository extends BaseRepository<Gallery, Long> {
}
