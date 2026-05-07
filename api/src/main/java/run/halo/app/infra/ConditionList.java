package run.halo.app.infra;

import java.util.*;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * This {@link ConditionList} to stores multiple {@link Condition}.
 *
 * <p>The element added after is always the first, the first to be removed is always the first to be added.
 *
 * <p>The queue head is the one whose element index is 0 Note that: this class is not thread-safe.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ConditionList extends AbstractCollection<Condition> {
    private static final int EVICT_THRESHOLD = 20;
    private final Deque<Condition> conditions = new LinkedList<>();

    @Override
    public boolean add(Condition condition) {
        if (isSame(conditions.peekFirst(), condition)) {
            return false;
        }
        return conditions.add(condition);
    }

    public boolean addFirst(Condition condition) {
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
    public boolean addAndEvictFIFO(Condition condition) {
        return addAndEvictFIFO(condition, EVICT_THRESHOLD);
    }

    /**
     * Add {@param #condition} and evict the first item if the size of conditions is greater than
     * {@param evictThreshold}.
     *
     * @param condition item to add
     */
    public boolean addAndEvictFIFO(Condition condition, int evictThreshold) {
        var current = getCondition(condition.getType());
        if (current != null) {
            // do not update last transition time if status is not changed
            if (Objects.equals(condition.getStatus(), current.getStatus())) {
                condition.setLastTransitionTime(current.getLastTransitionTime());
            }
        }

        conditions.remove(current);
        conditions.addFirst(condition);

        while (conditions.size() > evictThreshold) {
            removeLast();
        }
        return true;
    }

    private @Nullable Condition getCondition(String type) {
        for (Condition condition : conditions) {
            if (condition.getType().equals(type)) {
                return condition;
            }
        }
        return null;
    }

    public void remove(Condition condition) {
        conditions.remove(condition);
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by this deque (in other words, the first
     * element of this deque), or returns {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst()}.
     *
     * @return the head of the queue represented by this deque, or {@code null} if this deque is empty
     */
    public @Nullable Condition peek() {
        return peekFirst();
    }

    public @Nullable Condition peekFirst() {
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

    private boolean isSame(@Nullable Condition a, @Nullable Condition b) {
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
    public boolean equals(@Nullable Object o) {
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
