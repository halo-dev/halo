package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import run.halo.app.model.entity.VisitorLog;
import run.halo.app.model.entity.id.VisitorLogId;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.model.projection.VisitorLogRegionCountProjection;
import run.halo.app.repository.base.BaseRepository;

import java.util.Date;
import java.util.List;

/**
 * VisitorLog repository
 *
 * @author Holldean
 * @date 2020-05-13
 */
@Repository
public interface VisitorLogRepository extends BaseRepository<VisitorLog, VisitorLogId> {

    /**
     * Find a VisitorLog by accessDate and ipAddress.
     *
     * @param accessDate visitor's access date.
     * @param ipAddress visitor's ip address.
     * @return visitor ip log.
     */
    VisitorLog findByAccessDateAndIpAddress(Date accessDate, String ipAddress);

    /**
     * Find all VisitorLog after the start date.
     *
     * @param startDate the start date.
     * @return list of visitor ip log.
     */
    List<VisitorLog> findByAccessDateAfter(Date startDate);

    /**
     * Count VisitorLog group by date after the start date.
     *
     * @param startDate the start date.
     * @return list of date and count pair.
     */
    @Query("select new run.halo.app.model.projection.VisitorLogDayCountProjection(v.accessDate, count(distinct v.ipAddress)) from VisitorLog v " +
           "where v.accessDate >= :startDate group by v.accessDate order by v.accessDate")
    List<VisitorLogDayCountProjection> countByAccessDateAfterGroupByDay(@Param(value = "startDate") Date startDate);

    /**
     * Count VisitorLog group by country after the start date.
     *
     * @param startDate the start date.
     * @return list of country and count pair.
     */
    @Query("select new run.halo.app.model.projection.VisitorLogRegionCountProjection(v.country, count(v.ipAddress)) from VisitorLog v " +
           "where v.accessDate >= :startDate group by v.country order by v.country")
    List<VisitorLogRegionCountProjection> countByAccessDateAfterGroupByCountry(@Param(value = "startDate") Date startDate);

    /**
     * Count VisitorLog group by province after the start date.
     *
     * @param startDate the start date.
     * @return list of province and count pair.
     */
    @Query("select new run.halo.app.model.projection.VisitorLogRegionCountProjection(v.province, count(v.ipAddress)) from VisitorLog v " +
           "where v.accessDate >= :startDate and v.country = '中国' group by v.province order by v.province")
    List<VisitorLogRegionCountProjection> countByAccessDateAfterGroupByChineseProvince(@Param(value = "startDate") Date startDate);

    /**
     * Count VisitorLog group by month.
     *
     * @param startDate the start date.
     * @return list of month and count pair.
     */
    @Query("select new run.halo.app.model.projection.VisitorLogMonthCountProjection(FUNCTION('MONTH', v.accessDate), count(v.ipAddress)) from VisitorLog v " +
           "where v.accessDate >= :startDate group by FUNCTION('YEAR', v.accessDate), FUNCTION('MONTH', v.accessDate) order by FUNCTION('YEAR', v.accessDate), FUNCTION('MONTH', v.accessDate)")
    List<VisitorLogMonthCountProjection> countByAccessDateAfterGroupByMonth(@Param(value = "startDate") Date startDate);
}