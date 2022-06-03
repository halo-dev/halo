package run.halo.app.infra.utils;

import java.util.UUID;

/**
 * @author guqing
 * @date 2022-04-12
 */
public class HaloUtils {

    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
