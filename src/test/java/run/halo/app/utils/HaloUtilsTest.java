package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Halo utilities test.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-29
 */
@Slf4j
public class HaloUtilsTest {

    @Test
    public void timeFormatTest() {
        long seconds = 0;
        String timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("0 second"));

        seconds = -1;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("0 second"));

        seconds = 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("30 seconds"));

        seconds = 60;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("1 minute"));

        seconds = 120;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("2 minutes"));

        seconds = 3600;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("1 hour"));

        seconds = 7200;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("2 hours"));

        seconds = 7200 + 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("2 hours, 30 seconds"));

        seconds = 7200 + 60 + 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertThat(timeFormat, equalTo("2 hours, 1 minute, 30 seconds"));


    }

    @Test
    public void pluralizeTest() {

        String label = "chance";
        String pluralLabel = "chances";

        String pluralizedFormat = HaloUtils.pluralize(1, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("1 chance"));

        pluralizedFormat = HaloUtils.pluralize(2, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("2 chances"));

        pluralizedFormat = HaloUtils.pluralize(0, label, pluralLabel);
        assertThat(pluralizedFormat, equalTo("no chances"));

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
            assertThat(result, equalTo("no " + pluralLabel));
        });

    }

    @Test(expected = IllegalArgumentException.class)
    public void pluralizeLabelExceptionTest() {
        HaloUtils.pluralize(1, null, null);
    }

    @Test
    public void desensitizeSuccessTest() {
        String plainText = "12345678";

        String desensitization = HaloUtils.desensitize(plainText, 1, 1);
        assertThat(desensitization, equalTo("1******8"));

        desensitization = HaloUtils.desensitize(plainText, 2, 3);
        assertThat(desensitization, equalTo("12***678"));

        desensitization = HaloUtils.desensitize(plainText, 2, 6);
        assertThat(desensitization, equalTo("12345678"));

        desensitization = HaloUtils.desensitize(plainText, 2, 7);
        assertThat(desensitization, equalTo("12345678"));

        desensitization = HaloUtils.desensitize(plainText, 0, 0);
        assertThat(desensitization, equalTo("********"));

        desensitization = HaloUtils.desensitize(plainText, -1, -1);
        assertThat(desensitization, equalTo("********"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void desensitizeFailureTest() {
        String plainText = " ";
        HaloUtils.desensitize(plainText, 1, 1);
    }

    @Test
    public void compositeHttpUrl() {
        String url = HaloUtils.compositeHttpUrl("https://halo.run", "path1", "path2");
        assertEquals("https://halo.run/path1/path2", url);

        url = HaloUtils.compositeHttpUrl("https://halo.run/", "path1", "path2");
        assertEquals("https://halo.run/path1/path2", url);

        url = HaloUtils.compositeHttpUrl("https://halo.run/", "/path1", "path2");
        assertEquals("https://halo.run/path1/path2", url);

        url = HaloUtils.compositeHttpUrl("https://halo.run/", "/path1/", "path2");
        assertEquals("https://halo.run/path1/path2", url);

        url = HaloUtils.compositeHttpUrl("https://halo.run/", "/path1/", "/path2/");
        assertEquals("https://halo.run/path1/path2", url);
    }

    @Test
    public void normalizeUrl() {
        assertEquals("/2019/2/2/avatar.jpg", HaloUtils.normalizeUrl("/2019/2/2/avatar.jpg"));

        assertEquals("http://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("//cn.gravatar.com/avatar?d=mm"));

        assertEquals("http://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("cn.gravatar.com/avatar?d=mm"));

        assertEquals("https://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("https://cn.gravatar.com/avatar?d=mm"));
    }
}
