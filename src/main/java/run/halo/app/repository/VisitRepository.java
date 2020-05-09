package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.halo.app.model.entity.Visit;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

/**
 * Visit repository.
 *
 * @author chao19991005
 * @date 2020-04-21
 */

public interface VisitRepository extends BaseRepository<Visit, Integer> {

    /**
     * Counts visits today.
     *
     * @return total visits
     * @param day the day queried for
     * @param month the month queried for
     * @param year the year queried for
     */
    @Query("select count(v.visitId) from Visit v where day(v.createTime) = :day and month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitToday(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits yesterday method 1.
     *
     * @return total visits
     * @param day the day queried for
     * @param month the month queried for
     * @param year the year queried for
     */
    @Query("select count(v.visitId) from Visit v where day(v.createTime) = :day - 1 and month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitYesterday1(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits yesterday method 2.
     *
     * @return total visits
     * @param day the day queried for
     * @param month the month queried for
     * @param year the year queried for
     */
    @Query("select count(v.visitId) from Visit v where month(v.createTime) = :month - 1 and day(v.createTime) = :day and year(v.createTime) = :year")
    Long countVisitYesterday2(@Param("day") Integer day, @Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits this month.
     *
     * @return total visits
     * @param month the month queried for
     * @param year the year queried for
     */
    @Query("select count(v.visitId) from Visit v where month(v.createTime) = :month and year(v.createTime) = :year")
    Long countVisitThisMonth(@Param("month") Integer month, @Param("year") Integer year);

    /**
     * Counts visits this year.
     *
     * @return total visits
     * @param year the year queried for
     */
    @Query("select sum(v.visitId) from Visit v where year(v.createTime) = :year")
    Long countVisitThisYear(@Param("year") Integer year);

    /**
     * find all visitors' district(where they are)
     *
     * @return a list of string which are all the district
     */
    @Query("select v.visitorDistrict from Visit v group by v.visitorDistrict")
    List<String> findAllVisitorDistrict();

    /**
     * find visit number of each district
     *
     * @return a list of number which are all the visit number of each district
     */
    @Query("select count(v.visitId) from Visit v group by v.visitorDistrict")
    List<Long> findAllVisitorDistrictNumber();

}
