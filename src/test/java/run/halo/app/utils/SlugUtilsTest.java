package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * @author johnniang
 * @date 3/20/19
 */
@Slf4j
public class SlugUtilsTest {

    @Test
    public void makeSlugTest() {
        String slugResult = SlugUtils.slugify("Hello World");

        Assert.assertThat(slugResult, equalTo("hello-world"));
    }

    @Test
    public void slugTest() {
        String slug = SlugUtils.slug("一二三四 +/~!@#$%^&*()_+ - hello-world");

        Assert.assertThat(slug, equalTo("一二三四-hello-world"));
    }

    @Test
    public void nullSlugTest() {
        String slug = SlugUtils.slug("+/~!@#$%^&*()_+");

        log.debug("slug：" + slug);
    }
}
