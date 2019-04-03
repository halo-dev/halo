package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import static run.halo.app.model.support.HaloConst.OWO_MAP;

/**
 * Owo util
 *
 * @author : RYAN0UP
 * @date : 2017/12/22
 */
@Slf4j
public class OwoUtil {

    /**
     * Owo mark converted into a picture address
     *
     * @param content content
     * @return picture address
     */
    public static String parseOwo(String content) {
        if (CollectionUtils.isEmpty(OWO_MAP)) {
            return content;
        }

        for (String key : OWO_MAP.keySet()) {
            content = content.replace(key, OWO_MAP.get(key).toString());
        }
        return content;
    }
}
