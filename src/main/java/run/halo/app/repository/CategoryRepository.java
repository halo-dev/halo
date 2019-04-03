package run.halo.app.repository;

import run.halo.app.model.entity.Category;
import run.halo.app.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

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

    /**
     * Get category by slug name
     *
     * @param slugName slug name
     * @return Optional of Category
     */
    Optional<Category> getBySlugName(@NonNull String slugName);
}
