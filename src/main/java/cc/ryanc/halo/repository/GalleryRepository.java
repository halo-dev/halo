package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2018/2/26
 * @version : 1.0
 */
public interface GalleryRepository extends JpaRepository<Gallery,Long> {
}
