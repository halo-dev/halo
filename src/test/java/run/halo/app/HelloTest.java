package run.halo.app;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class HelloTest {
    @Test
    void test() {
        String s = "/tags";
        String s2 = "/tags/page/2";
        String s3 = "/tags/y/m/page/2";
        String s4 = "/tags/y/m";

        String path = s3;
        // PathPattern parse = PathPatternParser.defaultInstance.parse("/tags/{*pagePart}");
        // PathPattern.PathMatchInfo pathMatchInfo =
        //     parse.matchAndExtract(PathContainer.parsePath("/tags/page/1"));
        // System.out.println(pathMatchInfo);
        String[] split = StringUtils.split(path, "/");
        if (split.length > 1) {
            String pagePart = split[split.length - 2];
            if (pagePart.equals("page")) {
                String page = split[split.length - 1];
                split[split.length - 1] = String.valueOf(Integer.parseInt(split[split.length - 1]) + 1);
                System.out.println(ArrayUtils.toString(split));
            }
        }
    }

    private int
}
