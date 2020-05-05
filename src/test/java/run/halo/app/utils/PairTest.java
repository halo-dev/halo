package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Pair test.
 *
 * @author KristenLawrence
 * @date 20-5-3
 */
public class PairTest {

    @Test
    public void getSetTest() {
        Pair<Integer, String> pair = new Pair<>(100, "127.0.0.1");

        pair.setFirst(200);
        Assert.assertEquals(200, (int) pair.getFirst());

        pair.setLast("10.0.0.1");
        Assert.assertEquals("10.0.0.1", pair.getLast());
    }

    @Test
    public void equalTest() {
        Pair<Integer, String> pair1 = new Pair<>(100, "127.0.0.1");
        Pair<Integer, String> pair2 = new Pair<>(100, "127.0.0.1");

        Assert.assertEquals(pair1.hashCode(), pair2.hashCode());
        Assert.assertEquals(pair1, pair2);
    }

    @Test
    public void nullTest() {
        Pair<String, String> pair1 = new Pair<>(null, null);
        Pair<String, String> pair2 = new Pair<>(null, null);

        Assert.assertEquals(pair1.hashCode(), pair2.hashCode());
        Assert.assertEquals(pair1, pair2);
    }
}
