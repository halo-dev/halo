package run.halo.app.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * BeanUtils test.
 *
 * @author johnniang
 */
public class BeanUtilsTest {

    @Test
    public void transformFrom() {
        TestA a = new TestA(1, 2);

        TestC c = BeanUtils.transformFrom(a, TestC.class);
        assertEquals(a.getA(), c.getA());
        assertEquals(a.getB(), c.getB());

        TestB b = BeanUtils.transformFrom(a, TestB.class);
        assertEquals(a.getB(), b.getB());
        assertNull(b.getC());

        TestD d = new TestD(a);
        TestE e = BeanUtils.transformFrom(d, TestE.class);
        assertEquals(d.getA().getA(), e.getA().getA());
    }

    @Test
    public void transformFromInBatch() {
        TestA[] as = {
            new TestA(1, 2),
            new TestA(3, 4)
        };

        List<TestA> aList = Arrays.asList(as);

        List<TestC> cs = BeanUtils.transformFromInBatch(aList, TestC.class);
        assertEquals(as.length, cs.size());
        for (int i = 0; i < cs.size(); i++) {
            assertEquals(as[i].getA(), cs.get(i).getA());
            assertEquals(as[i].getB(), cs.get(i).getB());
        }

        List<TestB> bs = BeanUtils.transformFromInBatch(aList, TestB.class);
        assertEquals(as.length, bs.size());
        for (int i = 0; i < bs.size(); i++) {
            assertEquals(as[i].getB(), bs.get(i).getB());
            assertNull(bs.get(i).getC());
        }
    }

    @Test
    public void updateProperties() {
        TestA a = new TestA(1, 2);
        TestB b = new TestB(3, 4);
        TestC c = new TestC(5, 6);

        BeanUtils.updateProperties(a, b);
        assertEquals(b.getB(), a.getB());
        assertEquals(b.getC(), Integer.valueOf(4));

        BeanUtils.updateProperties(a, c);
        assertEquals(c.getA(), a.getA());
        assertEquals(c.getB(), a.getB());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestA {
        private Integer a;
        private Integer b;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestB {
        private Integer b;
        private Integer c;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestC {
        private Integer a;
        private Integer b;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestD {
        private TestA a;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestE {
        private TestA a;
    }
}
