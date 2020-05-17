package run.halo.app.listener.logger;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.logger.VisitorLogEvent;
import run.halo.app.service.VisitorLogService;

/**
 * VisitorLog event listener.
 *
 * @author Holldean
 * @date 2020-05-17
 */
@Component
public class VisitorLogEventListener {

    private final VisitorLogService visitorLogService;

    public VisitorLogEventListener(VisitorLogService visitorLogService) {
        this.visitorLogService = visitorLogService;
    }

    @EventListener
    @Async
    public void onApplicationEvent(VisitorLogEvent event) {
        // Create or update visitor log
        visitorLogService.createOrUpdate(event.getIpAddress());
    }
}
