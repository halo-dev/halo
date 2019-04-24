package run.halo.app.event.post;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract visit event listener.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class AbstractVisitEventListener {

//    private final Map<Integer, BlockingQueue<Integer>> postVisitQueueMap;
//
//    private final Map<Integer, PostVisitEventListener.PostVisitTask> postVisitTaskMap;
//
//    protected final BasePostRepository basePostRepository;
//
//    protected AbstractVisitEventListener(BasePostRepository basePostRepository) {
//        this.basePostRepository = basePostRepository;
//    }
//
//
//    /**
//     * Post visit task.
//     */
//    private class PostVisitTask implements Runnable {
//
//        private final Integer postId;
//
//        private PostVisitTask(Integer postId) {
//            this.postId = postId;
//        }
//
//        @Override
//        public void run() {
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    BlockingQueue<Integer> postVisitQueue = postVisitQueueMap.get(postId);
//                    Integer postId = postVisitQueue.take();
//
//                    log.debug("Took a new visit for post id: [{}]", postId);
//
//                    // Increase the visit
//                    postService.increaseVisit(postId);
//
//                    log.debug("Increased visits for post id: [{}]", postId);
//                } catch (InterruptedException e) {
//                    log.debug("Post visit task: " + Thread.currentThread().getName() + " was interrupted", e);
//                    // Ignore this exception
//                }
//            }
//
//            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
//        }
//    }
}
