package run.halo.app.infra.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author guqing
 * @date 2022-04-12
 */
public class HaloUtils {
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        return requestedWith == null || requestedWith.equalsIgnoreCase("XMLHttpRequest");
    }

    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
