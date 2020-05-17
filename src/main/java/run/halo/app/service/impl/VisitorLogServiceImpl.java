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
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.IpUtils;

import java.io.IOException;
import java.util.*;

/**
 * VisitorLog service implementation.
 *
 * @author Holldean
 * @date 2020-05-17
 */
@Slf4j
@Service
public class VisitorLogServiceImpl extends AbstractCrudService<VisitorLog, VisitorLogId> implements VisitorLogService {

    private final VisitorLogRepository visitorLogRepository;

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
        Date startDate = DateUtils.getDayAgo(days);
        VisitorLogRegionVo visitorLogRegionVo = new VisitorLogRegionVo();
        visitorLogRegionVo.setCountByCountry(visitorLogRepository.countByAccessDateAfterGroupByCountry(startDate));
        visitorLogRegionVo.setCountByProvince(visitorLogRepository.countByAccessDateAfterGroupByChineseProvince(startDate));
        return visitorLogRegionVo;
    }

    @Override
    public List<VisitorLogDayCountProjection> getVisitCountByDay(Integer days) {
        return visitorLogRepository.countByAccessDateAfterGroupByDay(DateUtils.getDayAgo(days));
    }

    @Override
    public List<VisitorLogMonthCountProjection> getVisitCountByMonth(Integer months) {
        return visitorLogRepository.countByAccessDateAfterGroupByMonth(DateUtils.getFistDayOfMonth(months));
    }

    @Override
    public void createOrUpdate(String ipAddress) {
        VisitorLog VisitorLog = getBy(DateUtils.now(), ipAddress);
        if (VisitorLog == null) {
            VisitorLog = new VisitorLog(ipAddress);
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

}
