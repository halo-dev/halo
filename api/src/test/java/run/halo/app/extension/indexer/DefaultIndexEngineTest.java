package run.halo.app.extension.indexer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.junit.jupiter.api.Test;

class DefaultIndexEngineTest {

    @Test
    void priorityQueueTest() {
        int n = 3;
        var pq = new PriorityQueue<>(n, Comparator.<Integer>naturalOrder().reversed());
        List.of(5, 4, 3, 2, 1).forEach(i -> {
            pq.offer(i);
            if (pq.size() > n) {
                pq.poll();
            }
        });
        var result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.addFirst(pq.poll());
        }
        result.forEach(System.out::println);
    }

}