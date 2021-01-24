package run.halo.app.utils;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author zhixiang.yuan
 * @since 2020/07/19 20:22:58
 */
@Slf4j
class MarkdownUtilsTest {

    @Test
    void removeFrontMatter() {
        String markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---";
        Assert.isTrue("".equals(MarkdownUtils.removeFrontMatter(markdown)));

        markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---"
            + "test";
        Assert.isTrue("test".equals(MarkdownUtils.removeFrontMatter(markdown)));

        markdown = "---\n"
            + "title: \"test remove\"\n"
            + "---"
            + "test---";
        Assert.isTrue("test---".equals(MarkdownUtils.removeFrontMatter(markdown)));
    }
}