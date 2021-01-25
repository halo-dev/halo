package run.halo.app.service.base;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.exception.NotFoundException;

/**
 * CrudService interface contains some common methods.
 *
 * @param <D> domain type
 * @param <I> id type
 * @author johnniang
 */
public interface CrudService<D, I> {


    /**
     * List All
     *
     * @return List
     */
    @NonNull
    List<D> listAll();

    /**
     * List all by sort
     *
     * @param sort sort
     * @return List
     */
    @NonNull
    List<D> listAll(@NonNull Sort sort);

    /**
     * List all by pageable
     *
     * @param pageable pageable
     * @return Page
     */
    @NonNull
    Page<D> listAll(@NonNull Pageable pageable);

    /**
     * List all by ids
     *
     * @param ids ids
     * @return List
     */
    @NonNull
    List<D> listAllByIds(@Nullable Collection<I> ids);

    /**
     * List all by ids and sort
     *
     * @param ids ids
     * @param sort sort
     * @return List
     */
    @NonNull
    List<D> listAllByIds(@Nullable Collection<I> ids, @NonNull Sort sort);

    /**
     * Fetch by id
     *
     * @param id id
     * @return Optional
     */
    @NonNull
    Optional<D> fetchById(@NonNull I id);

    /**
     * Get by id
     *
     * @param id id
     * @return DOMAIN
     * @throws NotFoundException If the specified id does not exist
     */
    @NonNull
    D getById(@NonNull I id);

    /**
     * Gets domain of nullable by id.
     *
     * @param id id
     * @return DOMAIN
     */
    @Nullable
    D getByIdOfNullable(@NonNull I id);

    /**
     * Exists by id.
     *
     * @param id id
     * @return boolean
     */
    boolean existsById(@NonNull I id);

    /**
     * Must exist by id, or throw NotFoundException.
     *
     * @param id id
     * @throws NotFoundException If the specified id does not exist
     */
    void mustExistById(@NonNull I id);

    /**
     * count all
     *
     * @return long
     */
    long count();

    /**
     * save by domain
     *
     * @param domain domain
     * @return DOMAIN
     */
    @NonNull
    @Transactional
    D create(@NonNull D domain);

    /**
     * save by domains
     *
     * @param domains domains
     * @return List
     */
    @NonNull
    @Transactional
    List<D> createInBatch(@NonNull Collection<D> domains);

    /**
     * Updates by domain
     *
     * @param domain domain
     * @return DOMAIN
     */
    @NonNull
    @Transactional
    D update(@NonNull D domain);

    /**
     * Flushes all pending changes to the database.
     */
    void flush();

    /**
     * Updates by domains
     *
     * @param domains domains
     * @return List
     */
    @NonNull
    @Transactional
    List<D> updateInBatch(@NonNull Collection<D> domains);

    /**
     * Removes by id
     *
     * @param id id
     * @return DOMAIN
     * @throws NotFoundException If the specified id does not exist
     */
    @NonNull
    @Transactional
    D removeById(@NonNull I id);

    /**
     * Removes by id if present.
     *
     * @param id id
     * @return DOMAIN
     */
    @Nullable
    @Transactional
    D removeByIdOfNullable(@NonNull I id);

    /**
     * Remove by domain
     *
     * @param domain domain
     */
    @Transactional
    void remove(@NonNull D domain);

    /**
     * Remove by ids
     *
     * @param ids ids
     */
    @Transactional
    void removeInBatch(@NonNull Collection<I> ids);

    /**
     * Remove all by domains
     *
     * @param domains domains
     */
    @Transactional
    void removeAll(@NonNull Collection<D> domains);

    /**
     * Remove all
     */
    @Transactional
    void removeAll();
}
