package run.halo.app.repository.base;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

/**
 * Base repository interface contains some common methods.
 *
 * @param <D> domain type
 * @param <I> id type
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
@NoRepositoryBean
public interface BaseRepository<D, I> extends JpaRepository<D, I> {

    /**
     * Finds all domain by id list.
     *
     * @param ids id list of domain must not be null
     * @param sort the specified sort must not be null
     * @return a list of domains
     */
    @NonNull
    List<D> findAllByIdIn(@NonNull Collection<I> ids, @NonNull Sort sort);

    /**
     * Finds all domain by domain id list.
     *
     * @param ids must not be null
     * @param pageable must not be null
     * @return a list of domains
     */
    @NonNull
    Page<D> findAllByIdIn(@NonNull Collection<I> ids, @NonNull Pageable pageable);

    /**
     * Deletes by id list.
     *
     * @param ids id list of domain must not be null
     * @return number of rows affected
     */
    long deleteByIdIn(@NonNull Collection<I> ids);

}
