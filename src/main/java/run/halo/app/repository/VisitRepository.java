package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.halo.app.model.entity.Visit;
import run.halo.app.repository.base.BaseRepository;

/**
 * Visit repository.
 *
 * @author chao19991005
 * @date 2020-04-21
 */

public interface VisitRepository extends BaseRepository<Visit, Integer> {

    /**
     * Counts visits today. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select count(v.visitId) from Visit v where day(v.createTime) = :day and month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitToday(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits yesterday method 1. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select count(v.visitId) from Visit v where day(v.createTime) = :day - 1 and month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitYesterday1(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits yesterday method 2. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select count(v.visitId) from Visit v where month(v.createTime) = :month - 1 and day(v.createTime) = :day and year(v.createTime) = :year")
    Long countVisitYesterday2(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits this month. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select count(v.visitId) from Visit v where month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitThisMonth(@Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits this year. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select sum(v.visitId) from Visit v where year(v.createTime) = :year")
    Long countVisitThisYear(@Param("year") Integer year);

}
