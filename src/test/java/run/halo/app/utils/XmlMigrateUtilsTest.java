package run.halo.app.utils;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import run.halo.app.handler.migrate.support.wordpress.Rss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author guqing
 * @date 2020-01-18 14:02
 */
public class XmlMigrateUtilsTest {
    @Test
    public void testXmlUtil() throws IOException, URISyntaxException {
        URL migrationUrl = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + "wordpressdemo.xml");
        URI wordpressUrl = migrationUrl.toURI();
        File file = new File(wordpressUrl);
        FileInputStream inputStream = new FileInputStream(file);
        String s = XmlMigrateUtils.xml2jsonString(inputStream);
        JSONObject json = JSONObject.parseObject(s);

        System.out.println("WordPress blog json data:" + json);

        Rss rss = json.getObject("rss", Rss.class);
        System.out.println(rss);
    }
}
