package run.halo.app.service;

import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.entity.Visit;
import run.halo.app.service.base.CrudService;

/**
 * Category service.
 *
 * @author chao19991005
 * @date 2020-04-21
 */
public interface VisitService extends CrudService<Visit, Integer> {

    /**
     * Counts visit today.
     *
     * @return today's visit
     */
    long countVisitToday();

    /**
     * Counts visit yesterday.
     *
     * @return yesterdays's visit
     */
    long countVisitYesterday();

    /**
     * Counts visit this month.
     *
     * @return this month's visit
     */
    long countVisitThisMonth();

    /**
     * Counts visit this year.
     *
     * @return this years's visit
     */
    long countVisitThisYear();
}
