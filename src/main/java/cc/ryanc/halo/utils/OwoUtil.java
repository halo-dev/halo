package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.dto.HaloConst;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 *     OwO表情工具类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/22
 */
@Slf4j
public class OwoUtil {

    /**
     * 将表情标志转化为图片地址
     *
     * @param mark 表情标志
     * @return 表情图片地址
     */
    public static String markToImg(String mark) {
        for (String key : HaloConst.OWO.keySet()) {
            mark = mark.replace(key, HaloConst.OWO.get(key));
        }
        return mark;
    }
}
