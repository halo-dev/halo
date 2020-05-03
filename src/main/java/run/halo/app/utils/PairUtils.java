package run.halo.app.utils;

/**
 * Pair utilities.
 *
 * @author KristenLawrence
 * @date 20-5-3
 */
public class PairUtils<V, K> {

    private V first;

    private K last;

    public PairUtils() {
        this.first = null;
        this.last  = null;
    }

    public PairUtils(V first, K last) {
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
    public int hashCode() {
        return first.hashCode() + last.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PairUtils)) {
            return false;
        }
        
        PairUtils<V, K> pair = (PairUtils<V,K>) object;
        
        return pair.first.equals(first) && pair.last.equals(last);
    }
}
