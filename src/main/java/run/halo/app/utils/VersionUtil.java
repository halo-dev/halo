package run.halo.app.utils;

import cn.hutool.core.util.StrUtil;

import java.util.StringTokenizer;

/**
 * @author ryanwang
 * @date 2020-02-03
 * @see com.sun.xml.internal.ws.util.VersionUtil
 */
public class VersionUtil {

    public VersionUtil() {
    }

    public static int[] getCanonicalVersion(String version) {
        int[] canonicalVersion = new int[]{1, 1, 0, 0};
        StringTokenizer tokenizer = new StringTokenizer(version, ".");
        String token = tokenizer.nextToken();
        canonicalVersion[0] = Integer.parseInt(token);
        token = tokenizer.nextToken();
        StringTokenizer subTokenizer;
        if (!token.contains(StrUtil.UNDERLINE)) {
            canonicalVersion[1] = Integer.parseInt(token);
        } else {
            subTokenizer = new StringTokenizer(token, "_");
            canonicalVersion[1] = Integer.parseInt(subTokenizer.nextToken());
            canonicalVersion[3] = Integer.parseInt(subTokenizer.nextToken());
        }

        if (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (!token.contains(StrUtil.UNDERLINE)) {
                canonicalVersion[2] = Integer.parseInt(token);
                if (tokenizer.hasMoreTokens()) {
                    canonicalVersion[3] = Integer.parseInt(tokenizer.nextToken());
                }
            } else {
                subTokenizer = new StringTokenizer(token, "_");
                canonicalVersion[2] = Integer.parseInt(subTokenizer.nextToken());
                canonicalVersion[3] = Integer.parseInt(subTokenizer.nextToken());
            }
        }

        return canonicalVersion;
    }

    public static int compare(String version1, String version2) {
        int[] canonicalVersion1 = getCanonicalVersion(version1);
        int[] canonicalVersion2 = getCanonicalVersion(version2);
        if (canonicalVersion1[0] < canonicalVersion2[0]) {
            return -1;
        } else if (canonicalVersion1[0] > canonicalVersion2[0]) {
            return 1;
        } else if (canonicalVersion1[1] < canonicalVersion2[1]) {
            return -1;
        } else if (canonicalVersion1[1] > canonicalVersion2[1]) {
            return 1;
        } else if (canonicalVersion1[2] < canonicalVersion2[2]) {
            return -1;
        } else if (canonicalVersion1[2] > canonicalVersion2[2]) {
            return 1;
        } else if (canonicalVersion1[3] < canonicalVersion2[3]) {
            return -1;
        } else {
            return canonicalVersion1[3] > canonicalVersion2[3] ? 1 : 0;
        }
    }

    /**
     * Compare version.
     *
     * @param current current version.
     * @param require require version.
     * @return true or false.
     */
    public static boolean compareVersion(String current, String require) {
        return compare(current, require) >= 0;
    }
}
