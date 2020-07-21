package run.halo.app.listener.post;

import lombok.NonNull;
import org.springframework.util.Assert;
import run.halo.app.event.post.AbstractVisitEvent;
import run.halo.app.handler.read.Read;

/**
 * @author: HeHui
 * @date: 2020-07-21 18:26
 * @description: Abstract smooth visit event listener.
 */
public class AbstractSmoothVisitEventListener {

    private final Read<Long, Integer> read;


    public AbstractSmoothVisitEventListener(Read<Long, Integer> read) {
        this.read = read;
    }


    /**
     * Handle visit event.
     *
     * @param event visit event must not be null
     * @throws InterruptedException
     */
    protected void handleVisitEvent(@NonNull AbstractVisitEvent event) throws InterruptedException {
        Assert.notNull(event, "Visit event must not be null");

        // Get post id
        Integer id = event.getId();
        read.read(id, 1L, null);

    }

}
