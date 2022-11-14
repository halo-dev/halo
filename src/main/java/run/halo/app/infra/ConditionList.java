package run.halo.app.infra;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.springframework.lang.NonNull;

/**
 * <p>This {@link ConditionList} to stores multiple {@link Condition}.</p>
 * Note that: this class is not thread-safe.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ConditionList implements Iterable<Condition> {
    private static final int EVICT_THRESHOLD = 20;
    private final Deque<Condition> conditions = new ArrayDeque<>();

    public void add(@NonNull Condition condition) {
        if (isSame(conditions.peek(), condition)) {
            return;
        }
        conditions.add(condition);
    }

    /**
     * Add {@param #condition} and evict the first item if the size of conditions is greater than
     * {@link #EVICT_THRESHOLD}.
     *
     * @param condition item to add
     */
    public void addAndEvictFIFO(@NonNull Condition condition) {
        this.add(condition);
        if (conditions.size() > EVICT_THRESHOLD) {
            conditions.removeFirst();
        }
    }

    /**
     * Add {@param #condition} and evict the first item if the size of conditions is greater than
     * {@param evictThreshold}.
     *
     * @param condition item to add
     */
    public void addAndEvictFIFO(@NonNull Condition condition, int evictThreshold) {
        this.add(condition);
        if (conditions.size() > evictThreshold) {
            conditions.removeFirst();
        }
    }

    public void remove(Condition condition) {
        conditions.remove(condition);
    }

    public void poll() {
        conditions.poll();
    }

    public Condition peek() {
        return conditions.peek();
    }

    public void clear() {
        conditions.clear();
    }

    public int size() {
        return conditions.size();
    }

    private boolean isSame(Condition a, Condition b) {
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getType(), b.getType())
            && Objects.equals(a.getStatus(), b.getStatus())
            && Objects.equals(a.getReason(), b.getReason())
            && Objects.equals(a.getMessage(), b.getMessage());
    }

    @Override
    public Iterator<Condition> iterator() {
        return conditions.iterator();
    }

    @Override
    public void forEach(Consumer<? super Condition> action) {
        conditions.forEach(action);
    }

    @Override
    public Spliterator<Condition> spliterator() {
        return Iterable.super.spliterator();
    }
}
