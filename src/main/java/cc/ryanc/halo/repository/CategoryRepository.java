package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2017/11/30
 * @version : 1.0
 * description: 分类目录持久层
 */
public interface CategoryRepository extends JpaRepository<Category,Integer>{

    /**
     * 根据分类目录路径查询，用于验证是否已经存在该路径
     * @param cateUrl cateUrl
     * @return category
     */
    Category findCategoryByCateUrl(String cateUrl);
}
