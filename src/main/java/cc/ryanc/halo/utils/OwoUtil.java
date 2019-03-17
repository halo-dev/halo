package cc.ryanc.halo.utils;

import lombok.extern.slf4j.Slf4j;

import static cc.ryanc.halo.model.support.HaloConst.OWO;

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
        for (String key : OWO.keySet()) {
            content = content.replace(key, OWO.get(key).toString());
        }
        return content;
    }
}
