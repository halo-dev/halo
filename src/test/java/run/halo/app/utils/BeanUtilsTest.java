package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 * BeanUtils test.
 *
 * @author johnniang
 */
class BeanUtilsTest {

    @Test
    void transformFrom() {
        TestA a = new TestA(1, 2);

        TestC c = BeanUtils.transformFrom(a, TestC.class);
        assertEquals(a.getAa(), Objects.requireNonNull(c).getAa());
        assertEquals(a.getBb(), c.getBb());

        TestB b = BeanUtils.transformFrom(a, TestB.class);
        assertEquals(a.getBb(), Objects.requireNonNull(b).getBb());
        assertNull(b.getCc());

        TestD d = new TestD(a);
        TestE e = BeanUtils.transformFrom(d, TestE.class);
        assertEquals(d.getAa().getAa(), Objects.requireNonNull(e).getAa().getAa());
    }

    @Test
    void transformFromInBatch() {
        TestA[] as = {
            new TestA(1, 2),
            new TestA(3, 4)
        };

        List<TestA> aList = Arrays.asList(as);

        List<TestC> cs = BeanUtils.transformFromInBatch(aList, TestC.class);
        assertEquals(as.length, cs.size());
        for (int i = 0; i < cs.size(); i++) {
            assertEquals(as[i].getAa(), cs.get(i).getAa());
            assertEquals(as[i].getBb(), cs.get(i).getBb());
        }

        List<TestB> bs = BeanUtils.transformFromInBatch(aList, TestB.class);
        assertEquals(as.length, bs.size());
        for (int i = 0; i < bs.size(); i++) {
            assertEquals(as[i].getBb(), bs.get(i).getBb());
            assertNull(bs.get(i).getCc());
        }
    }

    @Test
    void updateProperties() {
        TestA a = new TestA(1, 2);
        TestB b = new TestB(3, 4);
        TestC c = new TestC(5, 6);

        BeanUtils.updateProperties(a, b);
        assertEquals(b.getBb(), a.getBb());
        assertEquals(b.getCc(), Integer.valueOf(4));

        BeanUtils.updateProperties(a, c);
        assertEquals(c.getAa(), a.getAa());
        assertEquals(c.getBb(), a.getBb());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestA {
        private Integer aa;
        private Integer bb;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestB {
        private Integer bb;
        private Integer cc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestC {
        private Integer aa;
        private Integer bb;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestD {
        private TestA aa;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestE {
        private TestA aa;
    }
}
