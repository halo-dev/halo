package run.halo.app.infra.utils;

import java.util.UUID;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author guqing
 * @date 2022-04-12
 */
public class HaloUtils {

    public static boolean isAjaxRequest(ServerHttpRequest request) {
        String requestedWith = request.getHeaders().getFirst("x-requested-with");
        return requestedWith == null || requestedWith.equalsIgnoreCase("XMLHttpRequest");
    }

    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
