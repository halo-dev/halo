package run.halo.app.infra;

import java.util.AbstractCollection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.lang.NonNull;

/**
 * <p>This {@link ConditionList} to stores multiple {@link Condition}.</p>
 * <p>The element added after is always the first, the first to be removed is always the first to
 * be added.</p>
 * <p>The queue head is the one whose element index is 0</p>
 * Note that: this class is not thread-safe.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ConditionList extends AbstractCollection<Condition> {
    private static final int EVICT_THRESHOLD = 20;
    private final Deque<Condition> conditions = new LinkedList<>();

    @Override
    public boolean add(@NonNull Condition condition) {
        if (isSame(conditions.peekFirst(), condition)) {
            return false;
        }
        return conditions.add(condition);
    }

    public boolean addFirst(@NonNull Condition condition) {
        if (isSame(conditions.peekFirst(), condition)) {
            return false;
        }
        conditions.addFirst(condition);
        return true;
    }

    /**
     * Add {@param #condition} and evict the first item if the size of conditions is greater than
     * {@link #EVICT_THRESHOLD}.
     *
     * @param condition item to add
     */
    public boolean addAndEvictFIFO(@NonNull Condition condition) {
        return addAndEvictFIFO(condition, EVICT_THRESHOLD);
    }

    /**
     * Add {@param #condition} and evict the first item if the size of conditions is greater than
     * {@param evictThreshold}.
     *
     * @param condition item to add
     */
    public boolean addAndEvictFIFO(@NonNull Condition condition, int evictThreshold) {
        boolean result = this.addFirst(condition);
        while (conditions.size() > evictThreshold) {
            removeLast();
        }
        return result;
    }

    public void remove(Condition condition) {
        conditions.remove(condition);
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque (in other words, the first element of this deque), or
     * returns {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst()}.
     *
     * @return the head of the queue represented by this deque, or
     * {@code null} if this deque is empty
     */
    public Condition peek() {
        return peekFirst();
    }

    public Condition peekFirst() {
        return conditions.peekFirst();
    }

    public Condition removeLast() {
        return conditions.removeLast();
    }

    @Override
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConditionList that = (ConditionList) o;
        return Objects.equals(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions);
    }
}
