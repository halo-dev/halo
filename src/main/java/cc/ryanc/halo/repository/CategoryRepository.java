package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2017/11/30
 * @version : 1.0
 */
public interface CategoryRepository extends JpaRepository<Category,Long>{

    /**
     * 根据分类目录路径查询，用于验证是否已经存在该路径
     *
     * @param cateUrl cateUrl 文章url
     * @return category
     */
    Category findCategoryByCateUrl(String cateUrl);
}
