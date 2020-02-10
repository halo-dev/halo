package run.halo.app.controller.content;

import cn.hutool.core.util.URLUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Main controller Test.
 *
 * @author ryanwang
 * @date 2020-02-10
 */
public class MainControllerTest {

    @Test
    public void normalizeUrl() {
        Assert.assertEquals("https://halo.run/avatar", URLUtil.normalize("https://halo.run/avatar"));
        Assert.assertEquals("http://cn.gravatar.com/avatar/xxxxxxxxxxxxxxxxxxxxxxxxxs=256&d=mm", URLUtil.normalize("//cn.gravatar.com/avatar/xxxxxxxxxxxxxxxxxxxxxxxxxs=256&d=mm"));
        Assert.assertEquals("http://cn.gravatar.com/avatar/xxxxxxxxxxxxxxxxxxxxxxxxxs=256&d=mm", URLUtil.normalize("cn.gravatar.com/avatar/xxxxxxxxxxxxxxxxxxxxxxxxxs=256&d=mm"));
    }
}