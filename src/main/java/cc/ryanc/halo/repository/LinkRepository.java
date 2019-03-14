package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <pre>
 *     友情链接持久层
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
public interface LinkRepository extends BaseRepository<Link, Long> {
}
