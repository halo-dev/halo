package run.halo.app.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * CrudService interface contains some common methods.
 *
 * @param <DOMAIN> domain type
 * @param <ID>     id type
 * @author johnniang
 */
public interface CrudService<DOMAIN, ID> {


    /**
     * List All
     *
     * @return List
     */
    @NonNull
    List<DOMAIN> listAll();

    /**
     * List all by sort
     *
     * @param sort sort
     * @return List
     */
    @NonNull
    List<DOMAIN> listAll(@NonNull Sort sort);

    /**
     * List all by pageable
     *
     * @param pageable pageable
     * @return Page
     */
    @NonNull
    Page<DOMAIN> listAll(@NonNull Pageable pageable);

    /**
     * List all by ids
     *
     * @param ids ids
     * @return List
     */
    @NonNull
    List<DOMAIN> listAllByIds(@Nullable Collection<ID> ids);

    /**
     * List all by ids and sort
     *
     * @param ids  ids
     * @param sort sort
     * @return List
     */
    @NonNull
    List<DOMAIN> listAllByIds(@Nullable Collection<ID> ids, @NonNull Sort sort);

    /**
     * Fetch by id
     *
     * @param id id
     * @return Optional
     */
    @NonNull
    Optional<DOMAIN> fetchById(@NonNull ID id);

    /**
     * Get by id
     *
     * @param id id
     * @return DOMAIN
     * @throws NotFoundException If the specified id does not exist
     */
    @NonNull
    DOMAIN getById(@NonNull ID id);

    /**
     * Gets domain of nullable by id.
     *
     * @param id id
     * @return DOMAIN
     */
    @Nullable
    DOMAIN getByIdOfNullable(@NonNull ID id);

    /**
     * Exists by id.
     *
     * @param id id
     * @return boolean
     */
    boolean existsById(@NonNull ID id);

    /**
     * Must exist by id, or throw NotFoundException.
     *
     * @param id id
     * @throws NotFoundException If the specified id does not exist
     */
    void mustExistById(@NonNull ID id);

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
    DOMAIN create(@NonNull DOMAIN domain);

    /**
     * save by domains
     *
     * @param domains domains
     * @return List
     */
    @NonNull
    @Transactional
    List<DOMAIN> createInBatch(@NonNull Collection<DOMAIN> domains);

    /**
     * Updates by domain
     *
     * @param domain domain
     * @return DOMAIN
     */
    @NonNull
    @Transactional
    DOMAIN update(@NonNull DOMAIN domain);

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
    List<DOMAIN> updateInBatch(@NonNull Collection<DOMAIN> domains);

    /**
     * Removes by id
     *
     * @param id id
     * @return DOMAIN
     * @throws NotFoundException If the specified id does not exist
     */
    @NonNull
    @Transactional
    DOMAIN removeById(@NonNull ID id);

    /**
     * Removes by id if present.
     *
     * @param id id
     * @return DOMAIN
     */
    @Nullable
    @Transactional
    DOMAIN removeByIdOfNullable(@NonNull ID id);

    /**
     * Remove by domain
     *
     * @param domain domain
     */
    @Transactional
    void remove(@NonNull DOMAIN domain);

    /**
     * Remove by ids
     *
     * @param ids ids
     */
    @Transactional
    void removeInBatch(@NonNull Collection<ID> ids);

    /**
     * Remove all by domains
     *
     * @param domains domains
     */
    @Transactional
    void removeAll(@NonNull Collection<DOMAIN> domains);

    /**
     * Remove all
     */
    @Transactional
    void removeAll();
}
