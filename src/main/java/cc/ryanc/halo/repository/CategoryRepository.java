package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

/**
 * Category repository.
 *
 * @author johnniang
 */
public interface CategoryRepository extends BaseRepository<Category, Integer> {

    /**
     * Counts by category name.
     *
     * @param name category name must not be blank
     * @return the count
     */
    long countByName(@NonNull String name);

    /**
     * Counts by category id.
     *
     * @param id category id must not be null
     * @return the count
     */
    long countById(@NonNull Integer id);
}
