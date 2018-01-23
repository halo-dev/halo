package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.UserMeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @version :1.0
 * @date : 2017/11/14
 * description:
 */
public interface UserMetaRepository extends JpaRepository<UserMeta,Long>{

    UserMeta findByUserMetaKey(String key);
}
