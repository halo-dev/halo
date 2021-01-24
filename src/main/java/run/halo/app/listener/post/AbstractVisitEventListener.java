package run.halo.app.listener.post;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import run.halo.app.event.post.AbstractVisitEvent;
import run.halo.app.service.base.BasePostService;

/**
 * Abstract visit event listener.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class AbstractVisitEventListener {

    private final Map<Integer, BlockingQueue<Integer>> visitQueueMap;

    private final Map<Integer, PostVisitTask> visitTaskMap;

    private final BasePostService basePostService;

    private final ExecutorService executor;

    protected AbstractVisitEventListener(BasePostService basePostService) {
        this.basePostService = basePostService;

        int initCapacity = 8;

        long count = basePostService.count();

        if (count < initCapacity) {
            initCapacity = (int) count;
        }

        visitQueueMap = new ConcurrentHashMap<>(initCapacity << 1);
        visitTaskMap = new ConcurrentHashMap<>(initCapacity << 1);

        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Handle visit event.
     *
     * @param event visit event must not be null
     * @throws InterruptedException interrupted exception
     */
    protected void handleVisitEvent(@NonNull AbstractVisitEvent event) throws InterruptedException {
        Assert.notNull(event, "Visit event must not be null");

        // Get post id
        Integer id = event.getId();

        log.debug("Received a visit event, post id: [{}]", id);

        // Get post visit queue
        BlockingQueue<Integer> postVisitQueue =
            visitQueueMap.computeIfAbsent(id, this::createEmptyQueue);

        visitTaskMap.computeIfAbsent(id, this::createPostVisitTask);

        // Put a visit for the post
        postVisitQueue.put(id);
    }


    private PostVisitTask createPostVisitTask(Integer postId) {
        // Create new post visit task
        PostVisitTask postVisitTask = new PostVisitTask(postId);
        // Start a post visit task
        executor.execute(postVisitTask);

        log.debug("Created a new post visit task for post id: [{}]", postId);
        return postVisitTask;
    }

    private BlockingQueue<Integer> createEmptyQueue(Integer postId) {
        // Create a new queue
        return new LinkedBlockingQueue<>();
    }


    /**
     * Post visit task.
     */
    private class PostVisitTask implements Runnable {

        private final Integer id;

        private PostVisitTask(Integer id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BlockingQueue<Integer> postVisitQueue = visitQueueMap.get(id);
                    Integer postId = postVisitQueue.take();

                    log.debug("Took a new visit for post id: [{}]", postId);

                    // Increase the visit
                    basePostService.increaseVisit(postId);

                    log.debug("Increased visits for post id: [{}]", postId);
                } catch (InterruptedException e) {
                    log.debug(
                        "Post visit task: " + Thread.currentThread().getName() + " was interrupted",
                        e);
                    // Ignore this exception
                }
            }

            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
        }
    }
}
