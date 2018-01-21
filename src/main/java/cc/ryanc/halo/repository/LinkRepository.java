package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * className: LinkRepository
 * @author : RYAN0UP
 * @date : 2017/11/14
 * description: 友情链接持久层
 */
public interface LinkRepository extends JpaRepository<Link,Integer>{
}
