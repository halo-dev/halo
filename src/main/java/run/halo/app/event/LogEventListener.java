package run.halo.app.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.Log;
import run.halo.app.service.LogService;

/**
 * Log event listener.
 *
 * @author johnniang
 * @date 19-4-21
 */
@Component
public class LogEventListener implements ApplicationListener<LogEvent> {

    private final LogService logService;

    public LogEventListener(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void onApplicationEvent(LogEvent event) {
        // Convert to log
        Log logToCreate = event.getLogParam().convertTo();
        // Create log
        logService.create(logToCreate);
    }
}
