package run.halo.app.repository.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import run.halo.app.annotation.SensitiveConceal;

/**
 * Implementation of base repository.
 *
 * @param <DOMAIN> domain type
 * @param <ID> id type
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
@Slf4j
public class BaseRepositoryImpl<DOMAIN, ID> extends SimpleJpaRepository<DOMAIN, ID>
    implements BaseRepository<DOMAIN, ID> {

    private final JpaEntityInformation<DOMAIN, ID> entityInformation;

    private final EntityManager entityManager;

    public BaseRepositoryImpl(JpaEntityInformation<DOMAIN, ID> entityInformation,
        EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    /**
     * Executes a count query and transparently sums up all values returned.
     *
     * @param query must not be {@literal null}.
     * @return count
     */
    private static long executeCountQuery(TypedQuery<Long> query) {

        Assert.notNull(query, "TypedQuery must not be null!");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }

    /**
     * Finds all domain by id list and the specified sort.
     *
     * @param ids id list of domain must not be null
     * @param sort the specified sort must not be null
     * @return a list of domains
     */
    @Override
    @SensitiveConceal
    public List<DOMAIN> findAllByIdIn(Collection<ID> ids, Sort sort) {
        Assert.notNull(ids, "The given Collection of Id's must not be null!");
        Assert.notNull(sort, "Sort info must nto be null");

        log.debug("Customized findAllById method was invoked");

        if (!ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        if (entityInformation.hasCompositeId()) {
            List<DOMAIN> results = new ArrayList<>();
            ids.forEach(id -> super.findById(id).ifPresent(results::add));
            return results;
        }

        ByIdsSpecification<DOMAIN> specification = new ByIdsSpecification<>(entityInformation);
        TypedQuery<DOMAIN> query = super.getQuery(specification, sort);
        return query.setParameter(specification.parameter, ids).getResultList();
    }

    @Override
    @SensitiveConceal
    public Page<DOMAIN> findAllByIdIn(Collection<ID> ids, Pageable pageable) {
        Assert.notNull(ids, "The given Collection of Id's must not be null!");
        Assert.notNull(pageable, "Page info must nto be null");

        if (!ids.iterator().hasNext()) {
            return new PageImpl<>(Collections.emptyList());
        }

        if (entityInformation.hasCompositeId()) {
            throw new UnsupportedOperationException(
                "Unsupported find all by composite id with page info");
        }

        ByIdsSpecification<DOMAIN> specification = new ByIdsSpecification<>(entityInformation);
        TypedQuery<DOMAIN> query =
            super.getQuery(specification, pageable).setParameter(specification.parameter, ids);
        TypedQuery<Long> countQuery = getCountQuery(specification, getDomainClass())
            .setParameter(specification.parameter, ids);

        return pageable.isUnpaged()
            ? new PageImpl<>(query.getResultList())
            : readPage(query, getDomainClass(), pageable, countQuery);
    }

    /**
     * Deletes by id list.
     *
     * @param ids id list of domain must not be null
     * @return number of rows affected
     */
    @Override
    @Transactional
    public long deleteByIdIn(Collection<ID> ids) {

        log.debug("Customized deleteByIdIn method was invoked");
        // Find all domains
        List<DOMAIN> domains = findAllById(ids);

        // Delete in batch
        deleteInBatch(domains);

        // Return the size of domain deleted
        return domains.size();
    }

    protected <S extends DOMAIN> Page<S> readPage(TypedQuery<S> query, Class<S> domainClass,
        Pageable pageable, TypedQuery<Long> countQuery) {

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return PageableExecutionUtils.getPage(query.getResultList(), pageable,
            () -> executeCountQuery(countQuery));
    }

    private static final class ByIdsSpecification<T> implements Specification<T> {
        private static final long serialVersionUID = 1L;
        private final JpaEntityInformation<T, ?> entityInformation;
        @Nullable
        ParameterExpression<Collection> parameter;

        ByIdsSpecification(JpaEntityInformation<T, ?> entityInformation) {
            this.entityInformation = entityInformation;
        }

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            Path<?> path = root.get(this.entityInformation.getIdAttribute());
            this.parameter = cb.parameter(Collection.class);
            return path.in(this.parameter);
        }
    }
}
