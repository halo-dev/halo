package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description: 友情链接持久层
 */
public interface LinkRepository extends JpaRepository<Link,Long>{
}
