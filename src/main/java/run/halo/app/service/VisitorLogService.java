package run.halo.app.service;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.VisitorLog;
import run.halo.app.model.entity.id.VisitorLogId;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.model.vo.VisitorLogRegionVo;
import run.halo.app.service.base.CrudService;

import java.util.Date;
import java.util.List;

/**
 * VisitorLog service interface.
 *
 * @author Holldean
 * @date 2020-05-13
 */
public interface VisitorLogService extends CrudService<VisitorLog, VisitorLogId> {

    /**
     * Gets VisitorLog by access date and ip address.
     *
     * @param accessDate visitor's access date.
     * @param ipAddress  visitor's ip address.
     * @return visitor ip log.
     */
    VisitorLog getBy(@NonNull Date accessDate, @NonNull String ipAddress);

    /**
     * Gets ip count of a region from days before until now.
     *
     * @param days the number of days from now to the start date.
     * @return list of data and count pair.
     */
    VisitorLogRegionVo getVisitCountByRegion(@NonNull Integer days);

    /**
     * Gets ip count of a day from days before until now.
     *
     * @param days the number of days from now to the start date.
     * @return list of data and count pair.
     */
    List<VisitorLogDayCountProjection> getVisitCountByDay(@NonNull Integer days);

    /**
     * Gets ip count of a month.
     *
     * @param months the number of months from now.
     * @return list of month and count pair.
     */
    List<VisitorLogMonthCountProjection> getVisitCountByMonth(@NonNull Integer months);

    /**
     * Create VisitorLog or increase VisitorLog's count by one.
     *
     * @param accessDate visitor's access date.
     * @param ipAddress  visitor's ip address.
     */
    void createOrUpdate(@NonNull Date accessDate, @NonNull String ipAddress);

}
