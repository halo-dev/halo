package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @date : 2017/12/29
 * @version : 1.0
 * description: 环境常量
 */
public class HaloConst {
    /**
     * 所有设置选项（key,value）
     */
    public static Map<String,String> OPTIONS = new HashMap<>();

    /**
     * 用户信息
     */
    public static User USER = new User();

    /**
     * 所有文件
     */
    public static List<Attachment> ATTACHMENTS = new ArrayList<>();

    /**
     * 所有主题
     */
    public static List<Theme> THEMES = new ArrayList<>();
}
