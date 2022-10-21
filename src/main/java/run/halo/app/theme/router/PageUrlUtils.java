package run.halo.app.theme.router;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.utils.PathUtils;

/**
 * A utility class for template page url.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PageUrlUtils {
    public static final String PAGE_PART = "page";

    public static int pageNum(ServerRequest request) {
        if (isPageUrl(request.path())) {
            String pageNum = StringUtils.substringAfterLast(request.path(), "/page/");
            return NumberUtils.toInt(pageNum, 1);
        }
        return 1;
    }

    public static boolean isPageUrl(String path) {
        String[] split = StringUtils.split(path, "/");
        if (split.length > 1) {
            return PAGE_PART.equals(split[split.length - 2])
                && NumberUtils.isDigits(split[split.length - 1]);
        }
        return false;
    }

    public static long totalPage(ListResult<?> list) {
        return (list.getTotal() - 1) / list.getSize() + 1;
    }

    /**
     * Gets next page url with path.
     *
     * @param path request path
     * @return request path with next page part
     */
    public static String nextPageUrl(String path, long total) {
        String[] segments = StringUtils.split(path, "/");
        long defaultPage = Math.min(2, Math.max(total, 1));
        if (segments.length > 1) {
            String pagePart = segments[segments.length - 2];
            if (PAGE_PART.equals(pagePart)) {
                int pageNumIndex = segments.length - 1;
                String pageNum = segments[pageNumIndex];
                segments[pageNumIndex] = toNextPage(pageNum, total);
                return PathUtils.combinePath(segments);
            }
            return appendPagePart(PathUtils.combinePath(segments), defaultPage);
        }
        return appendPagePart(PathUtils.combinePath(segments), defaultPage);
    }

    /**
     * Gets previous page url with path.
     *
     * @param path request path
     * @return request path with previous page part
     */
    public static String prevPageUrl(String path) {
        String[] segments = StringUtils.split(path, "/");
        if (segments.length > 1) {
            String pagePart = segments[segments.length - 2];
            if (PAGE_PART.equals(pagePart)) {
                int pageNumIndex = segments.length - 1;
                String pageNum = segments[pageNumIndex];
                int prevPage = toPrevPage(pageNum);
                segments[pageNumIndex] = String.valueOf(prevPage);
                if (prevPage == 1) {
                    segments = ArrayUtils.subarray(segments, 0, pageNumIndex - 1);
                }
                if (segments.length == 0) {
                    return "/";
                }
                return PathUtils.combinePath(segments);
            }
        }
        return StringUtils.defaultString(path, "/");
    }

    private static String appendPagePart(String path, long page) {
        return PathUtils.combinePath(path, PAGE_PART, String.valueOf(page));
    }

    private static String toNextPage(String pageStr, long total) {
        long page = Math.min(parseInt(pageStr) + 1, Math.max(total, 1));
        return String.valueOf(page);
    }

    private static int toPrevPage(String pageStr) {
        return Math.max(parseInt(pageStr) - 1, 1);
    }

    private static int parseInt(String pageStr) {
        if (!NumberUtils.isParsable(pageStr)) {
            throw new IllegalArgumentException("Page number must be a number");
        }
        return NumberUtils.toInt(pageStr, 1);
    }
}
