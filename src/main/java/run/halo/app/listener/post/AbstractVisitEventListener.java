package run.halo.app.listener.post;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import run.halo.app.event.post.AbstractVisitEvent;
import run.halo.app.service.base.BasePostService;
import run.halo.app.utils.PairUtils;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Abstract visit event listener.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class AbstractVisitEventListener {

    private final Map<Integer, BlockingQueue<Integer>> visitQueueMap;

    private final Map<PairUtils<Integer, String>, PostVisitTask> visitTaskMap;

    private final Map<Integer, IpRecorder> visitIpMap;

    private final BasePostService basePostService;

    private final ExecutorService executor;

    private int initCapacity;

    protected AbstractVisitEventListener(BasePostService basePostService) {
        this.basePostService = basePostService;

        this.initCapacity = 8;

        long count = basePostService.count();

        if (count < initCapacity) {
            initCapacity = (int) count;
        }

        visitQueueMap = new ConcurrentHashMap<>(initCapacity << 1);
        visitTaskMap = new ConcurrentHashMap<>(initCapacity << 1);
        visitIpMap = new ConcurrentHashMap<>(initCapacity << 1);

        this.executor = Executors.newCachedThreadPool();
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

        // Get request ip address
        String ip = event.getIp();

        PairUtils<Integer, String> pair = new PairUtils<>(id, ip);
        log.debug("Received a visit event, post id: [{}], request ip address: [{}]", id, ip);

        // Get post visit queue
        BlockingQueue<Integer> postVisitQueue = visitQueueMap.computeIfAbsent(id, this::createEmptyQueue);

        visitIpMap.computeIfAbsent(id, this::createIpMap);

        visitTaskMap.computeIfAbsent(pair, this::createPostVisitTask);

        // Put a visit for the post
        postVisitQueue.put(id);
    }


    private PostVisitTask createPostVisitTask(PairUtils<Integer, String> pair) {
        int postId = pair.getFirst();
        String requestIp = pair.getLast();

        // Create new post visit task
        PostVisitTask postVisitTask = new PostVisitTask(postId, requestIp, this.visitIpMap.get(postId));
        // Start a post visit task
        executor.execute(postVisitTask);

        log.debug("Created a new post visit task for post id: [{}], from ip address: [{}]", postId, requestIp);
        return postVisitTask;
    }

    private BlockingQueue<Integer> createEmptyQueue(Integer postId) {
        // Create a new queue
        return new LinkedBlockingQueue<>();
    }

    private IpRecorder createIpMap(Integer postId) {
        // Create a new IpRecorder
        return new IpRecorder(postId, this.initCapacity);
    }


    /**
     * Post visit task.
     */
    private class PostVisitTask implements Runnable {

        private final Integer id;

        private final String ip;

        private IpRecorder ipRecorder;

        private PostVisitTask(Integer id) {
            this.id = id;
            this.ip = null;
            this.ipRecorder = null;
        }

        private PostVisitTask(Integer id, String ip, IpRecorder ipRecorder) {
            this.id = id;
            this.ip = ip;
            this.ipRecorder = ipRecorder;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BlockingQueue<Integer> postVisitQueue = visitQueueMap.get(id);
                    Integer postId = postVisitQueue.take();

                    log.debug("Took a new visit for post id: [{}], from ip address [{}]", postId, ip);

                    // If the ip and ipRecorder are not null and this ip hasn't appeared before, increase the visit
                    if (ip != null && ipRecorder != null && !ipRecorder.checkIp(this.ip)) {
                        basePostService.increaseVisit(postId);

                        log.debug("Increased visits for post id: [{}], from ip address [{}]", postId, ip);
                    }
                } catch (InterruptedException e) {
                    log.debug("Post visit task: " + Thread.currentThread().getName() + " was interrupted", e);
                    // Ignore this exception
                }
            }

            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
        }
    }

    /**
     *  Ip recorder
     */
    private class IpRecorder {

        private final Integer id;

        private final Map<String, Boolean> ipMap;

        private IpRecorder(int id, int initCapacity) {
            this.id = id;
            this.ipMap = new ConcurrentHashMap<>(initCapacity << 1);
        }

        /**
         * Check if the ip address has already visited before
         * @param ip ip address
         * @return whether the ip visited before
         */
        protected boolean checkIp(String ip) {
            if (this.ipMap.get(ip) == null) {
                ipMap.put(ip, true);
                return false;
            } else {
                return true;
            }
        }
    }
}
