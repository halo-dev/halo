package run.halo.app.event.post;

/**
 * Sheet visit event.
 *
 * @author johnniang
 * @date 19-4-24
 */
public class SheetVisitEvent extends AbstractVisitEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source  the object on which the event initially occurred (never {@code null})
     * @param requestIp the ip of visitor
     * @param sheetId sheet id must not be null
     */
    public SheetVisitEvent(Object source, String requestIp, Integer sheetId) {
        super(source, requestIp, sheetId);
    }
}
