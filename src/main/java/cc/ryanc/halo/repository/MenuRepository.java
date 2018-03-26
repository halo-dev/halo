package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/1/24
 * @version : 1.0
 * description :
 */
public interface MenuRepository extends JpaRepository<Menu,Long> {
}
