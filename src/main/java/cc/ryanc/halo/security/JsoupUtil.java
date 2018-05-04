package cc.ryanc.halo.security;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/5/4
 */
public class JsoupUtil {

    /**
     * 白名单
     */
    private static final Whitelist whitelist = Whitelist.basicWithImages();

    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    static {
        whitelist.addAttributes(":all", "style");
    }

    public static String clean(String content) {
        if (StringUtils.isNotBlank(content)) {
            content = content.trim();
        }
        return Jsoup.clean(content, "", whitelist, outputSettings);
    }
}
