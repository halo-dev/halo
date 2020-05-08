package run.halo.app.listener.post;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.model.entity.SheetVisitIp;
import run.halo.app.service.SheetService;
import run.halo.app.service.SheetVisitIpService;

/**
 * Sheet visit event listener.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Component
public class SheetVisitEventListener extends AbstractVisitEventListener {

    private final SheetVisitIpService sheetVisitIpService;

    protected SheetVisitEventListener(SheetService sheetService, SheetVisitIpService sheetVisitIpService) {
        super(sheetService);
        this.sheetVisitIpService = sheetVisitIpService;
    }

    @Async
    @EventListener
    public void onSheetVisitEvent(SheetVisitEvent event) throws InterruptedException {
        handleVisitEvent(event);
    }

    /**
     * Check ip record by sheet id and request ip.
     *
     * @param sheetId    sheet id
     * @param requestIp request ip
     * @return whether ip has appeared previously
     */
    @Override
    public boolean checkIpRecord(Integer sheetId, String requestIp) {
        boolean appeared = sheetVisitIpService.checkIpRecordByPostIdAndIp(sheetId, requestIp);
        if (!appeared) {
            SheetVisitIp visitIp = new SheetVisitIp();
            visitIp.setPostId(sheetId);
            visitIp.setIpAddress(requestIp);
            sheetVisitIpService.createBy(visitIp);
            return false;
        } else {
            return true;
        }
    }
}
