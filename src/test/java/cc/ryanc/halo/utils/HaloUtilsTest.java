package cc.ryanc.halo.utils;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Halo utilities test.
 *
 * @author johnniang
 * @date 3/29/19
 */
public class HaloUtilsTest {

    @Test
    public void pluralizeTest() {

        String label = "chance";
        String pluralLabel = "chances";

        String pluralizedFormat = HaloUtils.pluralize(1, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("1 chance"));

        pluralizedFormat = HaloUtils.pluralize(2, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("2 chances"));

        pluralizedFormat = HaloUtils.pluralize(0, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("no chance"));

        // Test random positive time
        IntStream.range(0, 10000).forEach(i -> {
            long time = RandomUtils.nextLong(2, Long.MAX_VALUE);
            String result = HaloUtils.pluralize(time, label, pluralLabel);
            assertThat(result, equalTo(time + " " + pluralLabel));
        });

        // Test random negative time
        IntStream.range(0, 10000).forEach(i -> {
            long time = (-1) * RandomUtils.nextLong();
            String result = HaloUtils.pluralize(time, label, pluralLabel);
            assertThat(result, equalTo("no " + label));
        });

    }

    @Test(expected = IllegalArgumentException.class)
    public void pluralizeLabelExceptionTest() {
        HaloUtils.pluralize(1, null, null);
    }
}