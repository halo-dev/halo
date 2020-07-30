package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Halo utilities test.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-29
 */
@Slf4j
class HaloUtilsTest {

    @Test
    void timeFormatTest() {
        long seconds = 0;
        String timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("0 second", timeFormat);

        seconds = -1;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("0 second", timeFormat);

        seconds = 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("30 seconds", timeFormat);

        seconds = 60;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("1 minute", timeFormat);

        seconds = 120;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("2 minutes", timeFormat);

        seconds = 3600;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("1 hour", timeFormat);

        seconds = 7200;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("2 hours", timeFormat);

        seconds = 7200 + 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("2 hours, 30 seconds", timeFormat);

        seconds = 7200 + 60 + 30;
        timeFormat = HaloUtils.timeFormat(seconds);
        assertEquals("2 hours, 1 minute, 30 seconds", timeFormat);
    }

    @Test
    void pluralizeTest() {

        String label = "chance";
        String pluralLabel = "chances";

        String pluralizedFormat = HaloUtils.pluralize(1, label, pluralLabel);
        assertEquals("1 chance", pluralizedFormat);


        pluralizedFormat = HaloUtils.pluralize(2, label, pluralLabel);
        assertEquals("2 chances", pluralizedFormat);

        pluralizedFormat = HaloUtils.pluralize(0, label, pluralLabel);
        assertEquals("no chances", pluralizedFormat);

        // Test random positive time
        IntStream.range(0, 10000).forEach(i -> {
            long time = RandomUtils.nextLong(2, Long.MAX_VALUE);
            String result = HaloUtils.pluralize(time, label, pluralLabel);
            assertEquals(time + " " + pluralLabel, result);
        });

        // Test random negative time
        IntStream.range(0, 10000).forEach(i -> {
            long time = (-1) * RandomUtils.nextLong();
            String result = HaloUtils.pluralize(time, label, pluralLabel);
            assertEquals("no " + pluralLabel, result);
        });
    }

    @Test
    void pluralizeLabelExceptionTest() {
        assertThrows(IllegalArgumentException.class, () -> HaloUtils.pluralize(1, null, null));
    }

    @Test
    void desensitizeSuccessTest() {
        String plainText = "12345678";

        String desensitization = HaloUtils.desensitize(plainText, 1, 1);
        assertEquals("1******8", desensitization);

        desensitization = HaloUtils.desensitize(plainText, 2, 3);
        assertEquals("12***678", desensitization);

        desensitization = HaloUtils.desensitize(plainText, 2, 6);
        assertEquals("12345678", desensitization);

        desensitization = HaloUtils.desensitize(plainText, 2, 7);
        assertEquals("12345678", desensitization);

        desensitization = HaloUtils.desensitize(plainText, 0, 0);
        assertEquals("********", desensitization);

        desensitization = HaloUtils.desensitize(plainText, -1, -1);
        assertEquals("********", desensitization);
    }

    @Test
    void desensitizeFailureTest() {
        String plainText = " ";
        assertThrows(IllegalArgumentException.class, () -> HaloUtils.desensitize(plainText, 1, 1));
    }

    @Test
    void compositeHttpUrl() {
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
    void normalizeUrl() {
        assertEquals("/2019/2/2/avatar.jpg", HaloUtils.normalizeUrl("/2019/2/2/avatar.jpg"));

        assertEquals("http://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("//cn.gravatar.com/avatar?d=mm"));

        assertEquals("http://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("cn.gravatar.com/avatar?d=mm"));

        assertEquals("https://cn.gravatar.com/avatar?d=mm", HaloUtils.normalizeUrl("https://cn.gravatar.com/avatar?d=mm"));
    }
}
