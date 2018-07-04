package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     公共常量
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/29
 */
public class HaloConst {

    /**
     * 所有设置选项（key,value）
     */
    public static Map<String, String> OPTIONS = new HashMap<>();

    /**
     * 所有文件
     */
    public static List<Attachment> ATTACHMENTS = new ArrayList<>();

    /**
     * 所有主题
     */
    public static List<Theme> THEMES = new ArrayList<>();

    /**
     * user_session
     */
    public static String USER_SESSION_KEY = "user_session";
}
