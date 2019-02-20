package cc.ryanc.halo.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    // **************** Select

    @NonNull
    List<DOMAIN> listAll();

    @NonNull
    List<DOMAIN> listAll(@NonNull Sort sort);

    @NonNull
    Page<DOMAIN> listAll(@NonNull Pageable pageable);

    @NonNull
    List<DOMAIN> listAllByIds(@NonNull Collection<ID> ids);

    @NonNull
    List<DOMAIN> listAllByIds(@NonNull Collection<ID> ids, @NonNull Sort sort);

    @NonNull
    Optional<DOMAIN> fetchById(@NonNull ID id);

    @NonNull
    DOMAIN getById(@NonNull ID id);

    @Nullable
    DOMAIN getNullableById(@NonNull ID id);

    boolean existsById(@NonNull ID id);

    void mustExistById(@NonNull ID id);

    long count();

    // **************** Create
    @NonNull
    DOMAIN create(@NonNull DOMAIN domain);

    @NonNull
    List<DOMAIN> createInBatch(@NonNull Collection<DOMAIN> domains);


    // **************** Update
    @NonNull
    DOMAIN update(@NonNull DOMAIN domain);

    @NonNull
    List<DOMAIN> updateInBatch(@NonNull Collection<DOMAIN> domains);


    // **************** Delete
    @NonNull
    DOMAIN removeById(@NonNull ID id);

    void remove(@NonNull DOMAIN domain);

    void removeInBatch(@NonNull Collection<ID> ids);

    void removeAll(@NonNull Collection<DOMAIN> domains);

    void removeAll();
}
