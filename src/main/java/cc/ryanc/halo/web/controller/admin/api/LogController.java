package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.LogOutputDTO;
import cc.ryanc.halo.service.LogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Log controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * List latest logs.
     *
     * @param top top
     * @return List of logs
     */
    @GetMapping("latest")
    @ApiOperation("Pages latest logs")
    public List<LogOutputDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return logService.pageLatest(top).getContent();
    }

    /**
     * Clear all logs.
     */
    @GetMapping("clear")
    @ApiOperation("Clear all logs")
    public void clear() {
        logService.removeAll();
    }
}
