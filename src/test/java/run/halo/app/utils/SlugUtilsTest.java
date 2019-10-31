package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.*;

/**
 * @author johnniang
 * @date 3/20/19
 */
public class SlugUtilsTest {

    @Test
    public void makeSlugTest() {
        String slugResult = SlugUtils.slugify("Hello World");

        Assert.assertThat(slugResult, equalToIgnoringCase("hello-world"));
    }
}
