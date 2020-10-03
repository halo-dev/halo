package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author johnniang
 * @date 3/20/19
 */
@Slf4j
public class SlugUtilsTest {

    @Test
    public void makeSlugTest() {
        String slugResult = SlugUtils.slugify("Hello World");

        Assertions.assertEquals("hello-world", slugResult);
    }

    @Test
    public void slugTest() {
        String slug = SlugUtils.slug("一二三四 +/~!@#$%^&*()_+ - hello-world");

        Assertions.assertEquals("一二三四-hello-world", slug);
    }

    @Test
    public void nullSlugTest() {
        String slug = SlugUtils.slug("+/~!@#$%^&*()_+");

        log.debug("slug：" + slug);
    }
}
