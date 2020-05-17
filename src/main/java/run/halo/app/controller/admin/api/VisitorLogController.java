package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.VisitorLogDTO;
import run.halo.app.model.entity.VisitorLog;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.model.vo.VisitorLogRegionVo;
import run.halo.app.service.VisitorLogService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * VisitorLog controller.
 *
 * @author Holldean
 * @date 2020-05-14
 */
@RestController
@RequestMapping("/api/admin/visits")
public class VisitorLogController {

    private VisitorLogService visitorLogService;

    public VisitorLogController(VisitorLogService visitorLogService) { this.visitorLogService = visitorLogService; }

    @GetMapping
    @ApiOperation("Lists visitor logs")
    public Page<VisitorLogDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        Page<VisitorLog> visitorLogPage = visitorLogService.listAll(pageable);
        return visitorLogPage.map(visitorLog -> new VisitorLogDTO().convertFrom(visitorLog));
    }

    @GetMapping("region")
    @ApiOperation("Count visits for map in a year")
    public VisitorLogRegionVo countVisitsByRegion() {
        return visitorLogService.getVisitCountByRegion(360);
    }

    @GetMapping("day")
    @ApiOperation("Count visits by day")
    public List<VisitorLogDayCountProjection> countVisitsByDay(@RequestParam(name = "days") Integer days) {
        return visitorLogService.getVisitCountByDay(days);
    }

    @GetMapping("month")
    @ApiOperation("Count visits by month")
    public List<VisitorLogMonthCountProjection> countVisitsByMonth(Integer months) {
        return visitorLogService.getVisitCountByMonth(months);
    }

}
