package run.halo.app.theme.router;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.utils.PathUtils;

/**
 * The {@link TemplateRouterStrategy} for generate {@link RouterFunction} specific to the template.
 *
 * @author guqing
 * @since 2.0.0
 */
@FunctionalInterface
public interface TemplateRouterStrategy {

    RouterFunction<ServerResponse> getRouteFunction(String template, String pattern);

    default int pageNum(ServerRequest request) {
        String pageNum = request.pathVariables().get(PageUrlUtils.PAGE_PART);
        return NumberUtils.toInt(pageNum, 1);
    }

    default long totalPage(ListResult<?> list) {
        return (list.getTotal() - 1) / list.getSize() + 1;
    }

    class PageUrlUtils {
        public static final String PAGE_PART = "page";

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
}
