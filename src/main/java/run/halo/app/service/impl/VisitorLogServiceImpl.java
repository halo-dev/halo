package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.VisitorLog;
import run.halo.app.model.entity.id.VisitorLogId;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.model.vo.VisitorLogRegionVo;
import run.halo.app.repository.VisitorLogRepository;
import run.halo.app.service.*;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.IpUtils;

import java.io.IOException;
import java.util.*;

/**
 * Admin service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-29
 */
@Slf4j
@Service
public class VisitorLogServiceImpl extends AbstractCrudService<VisitorLog, VisitorLogId> implements VisitorLogService {

    private VisitorLogRepository visitorLogRepository;

    protected VisitorLogServiceImpl(VisitorLogRepository visitorLogRepository) {
        super(visitorLogRepository);
        this.visitorLogRepository = visitorLogRepository;
    }

    @Override
    public VisitorLog getBy(Date accessDate, String ipAddress) {
        return visitorLogRepository.findByAccessDateAndIpAddress(accessDate, ipAddress);
    }

    @Override
    public VisitorLogRegionVo getVisitCountByRegion(Integer days) {
        Date startDate = getStartDate(days);
        VisitorLogRegionVo visitorLogRegionVo = new VisitorLogRegionVo();
        visitorLogRegionVo.setCountByCountry(visitorLogRepository.countByAccessDateAfterGroupByCountry(startDate));
        visitorLogRegionVo.setCountByProvince(visitorLogRepository.countByAccessDateAfterGroupByChineseProvince(startDate));
        return visitorLogRegionVo;
    }

    @Override
    public List<VisitorLogDayCountProjection> getVisitCountByDay(Integer days) {
        System.out.println(getStartDate(days));
        return visitorLogRepository.countByAccessDateAfterGroupByDay(getStartDate(days));
    }

    @Override
    public List<VisitorLogMonthCountProjection> getVisitCountByMonth(Integer months) {
        return visitorLogRepository.countByAccessDateAfterGroupByMonth(getFistDayOfMonth(months));
    }

    @Override
    public void createOrUpdate(Date accessDate, String ipAddress) {
        VisitorLog VisitorLog = getBy(accessDate, ipAddress);
        if (VisitorLog == null) {
            VisitorLog = new VisitorLog(accessDate, ipAddress);
            try {
                IpUtils.IpRegion region = IpUtils.getRegion(ipAddress);
                VisitorLog.setCountry(region.getCountry());
                VisitorLog.setProvince(region.getProvince());
                VisitorLog.setCity(region.getCity());
                VisitorLog.setISP(region.getISP());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            VisitorLog.setCount(VisitorLog.getCount() + 1);
        }
        visitorLogRepository.save(VisitorLog);
    }

    private Date getStartDate(Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // reset time
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // get start date
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    private Date getFistDayOfMonth(Integer months) {
        Calendar calendar = Calendar.getInstance();
        // reset time
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // get first day of that month
        calendar.add(Calendar.MONTH, -months);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }
}
