package run.halo.app.event.post;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.service.PostService;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Visit event listener.
 *
 * @author johnniang
 * @date 19-4-22
 */
@Slf4j
@Component
public class VisitEventListener {

    private final Map<Integer, BlockingQueue<Integer>> postVisitQueueMap;

    private final Map<Integer, PostVisitTask> postVisitTaskMap;

    private final PostService postService;

    private final ExecutorService executor;

    public VisitEventListener(PostService postService) {
        this.postService = postService;

        int initCapacity = 8;

        long count = postService.count();

        if (count < initCapacity) {
            initCapacity = (int) count;
        }

        postVisitQueueMap = new ConcurrentHashMap<>(initCapacity << 1);
        postVisitTaskMap = new ConcurrentHashMap<>(initCapacity << 1);

        this.executor = Executors.newCachedThreadPool();
    }

    @Async
    @EventListener
    public void onApplicationEvent(VisitEvent event) throws InterruptedException {
        // Get post id
        Integer postId = event.getPostId();

        log.debug("Received a visit event, post id: [{}]", postId);

        // Get post visit queue
        BlockingQueue<Integer> postVisitQueue = postVisitQueueMap.computeIfAbsent(postId, this::createEmptyQueue);

        postVisitTaskMap.computeIfAbsent(postId, this::createPostVisitTask);

        // Put a visit for the post
        postVisitQueue.put(postId);

        // TODO Attempt to manage the post visit tasks
    }

    private PostVisitTask createPostVisitTask(Integer postId) {
        // Create new post visit task
        PostVisitTask postVisitTask = new PostVisitTask(postId);
        // Start a post visit task
        executor.execute(postVisitTask);

        log.debug("Created a new post visit task for post id: [{}]", postId);
        return postVisitTask;
    }

    @PreDestroy
    protected void preDestroy() {
        executor.shutdownNow();
    }

    private BlockingQueue<Integer> createEmptyQueue(Integer postId) {
        // Create a new queue
        return new LinkedBlockingQueue<>();
    }

    /**
     * Post visit task.
     */
    private class PostVisitTask implements Runnable {

        private final Integer postId;

        private PostVisitTask(Integer postId) {
            this.postId = postId;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BlockingQueue<Integer> postVisitQueue = postVisitQueueMap.get(postId);
                    Integer postId = postVisitQueue.take();

                    log.debug("Took a new visit for post id: [{}]", postId);

                    // Increase the visit
                    postService.increaseVisit(postId);

                    log.debug("Increased visits for post id: [{}]", postId);
                } catch (InterruptedException e) {
                    log.debug("Post visit task: " + Thread.currentThread().getName() + " was interrupted", e);
                    // Ignore this exception
                }
            }

            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
        }
    }
}
