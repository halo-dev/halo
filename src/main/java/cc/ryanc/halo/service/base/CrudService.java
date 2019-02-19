package cc.ryanc.halo.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
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

    List<DOMAIN> listAll();

    List<DOMAIN> listAll(Sort sort);

    Page<DOMAIN> listAll(Pageable pageable);

//    <P> List<P> listAll(Class<P> projectionType);
//
//    <P> List<P> listAll(Class<P> projectionType, Sort sort);
//
//    <P> Page<P> listAll(Class<P> projectionType, Pageable pageable);

    List<DOMAIN> listAllByIds(Iterable<ID> ids);

    List<DOMAIN> listAllByIds(Iterable<ID> ids, Sort sort);

    Map<ID, DOMAIN> listAllByIdsAsMap(Iterable<ID> ids);

    Map<ID, DOMAIN> listAllByIdsAsMap(Iterable<ID> ids, Sort sort);

    Optional<DOMAIN> fetchById(ID id);

    DOMAIN getById(ID id);

    DOMAIN getNullableById(ID id);

    boolean existsById(ID id);

    void mustExistById(ID id);


    // **************** Create
    DOMAIN create(DOMAIN domain);

    List<DOMAIN> createInBatch(Iterable<DOMAIN> domains);


    // **************** Update

    DOMAIN update(DOMAIN domain);

    List<DOMAIN> updateInBatch(Iterable<DOMAIN> domains);


    // **************** Delete

    void removeById(ID id);

    void remove(DOMAIN domain);

    void removeInBatch(Iterable<ID> ids);

    void removeAll(Iterable<DOMAIN> domains);

}
