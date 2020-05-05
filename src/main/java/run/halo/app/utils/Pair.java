package run.halo.app.utils;

import java.util.Objects;

/**
 * Pair class.
 *
 * @author KristenLawrence
 * @date 20-5-3
 */
public class Pair<V, K> {

    private V first;

    private K last;

    public Pair() {
        this.first = null;
        this.last  = null;
    }

    public Pair(V first, K last) {
        this.first = first;
        this.last = last;
    }

    public V getFirst() {
        return this.first;
    }

    public void setFirst(V first) {
        this.first = first;
    }

    public K getLast() {
        return this.last;
    }

    public void setLast(K last) {
        this.last = last;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
            Objects.equals(last, pair.last);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }
}
