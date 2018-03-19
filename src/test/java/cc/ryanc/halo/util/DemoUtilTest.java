package cc.ryanc.halo.util;

import org.junit.Test;

/**
 * @author : RYAN0UP
 * @date : 2017/12/26
 * @version : 1.0
 * description:
 */
public class DemoUtilTest {

    @Test
    public void testZip(){
        HaloUtil.unZip("/Users/ryan0up/Desktop/adminlog.html.zip","/Users/ryan0up/Desktop/");
    }
}