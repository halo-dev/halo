package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.LogDTO;
import run.halo.app.model.entity.Log;
import run.halo.app.service.LogService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Log controller.
 *
 * @author johnniang
 * @date 2019-03-19
 */
@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest logs")
    public List<LogDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return logService.pageLatest(top).getContent();
    }

    @GetMapping
    @ApiOperation("Lists logs")
    public Page<LogDTO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        Page<Log> logPage = logService.listAll(pageable);
        return logPage.map(log -> new LogDTO().convertFrom(log));
    }

    @GetMapping("clear")
    @ApiOperation("Clears all logs")
    public void clear() {
        logService.removeAll();
    }
}
