package cc.ryanc.halo.model.dto;

/**
 * <pre>
 *     日志常量
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/19
 */
public interface LogsRecord {

    String INSTALL = "初始化博客";

    String LOGIN = "登录后台";

    String LOGIN_SUCCESS = "登录成功";

    String LOGIN_ERROR = "登录失败";

    String LOGOUT = "退出登录";

    String PUSH_POST = "发表文章";

    String PUSH_PAGE = "发表页面";

    String REMOVE_POST = "删除文章";

    String CHANGE_THEME = "更换主题";

    String UPLOAD_THEME = "上传主题";

    String UPLOAD_FILE = "上传附件";

    String REMOVE_FILE = "移除附件";
}
