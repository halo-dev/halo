package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     常用博客设置enum
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/14
 */
public enum BlogPropertiesEnum {

    /**
     * 博客语言
     */
    BLOG_LOCALE("blog_locale"),

    /**
     * 博客标题
     */
    BLOG_TITLE("blog_title"),

    /**
     * 博客地址
     */
    BLOG_URL("blog_url"),

    /**
     * 文章摘要字数
     */
    POST_SUMMARY("post_summary"),

    /**
     * 首页文章条数
     */
    INDEX_POSTS("index_posts"),

    /**
     * 每页评论条数
     */
    INDEX_COMMENTS("index_comments"),

    /**
     * 是否已经安装
     */
    IS_INSTALL("is_install"),

    /**
     * RSS显示文章条数
     */
    RSS_POSTS("rss_posts"),

    /**
     * API状态
     */
    API_STATUS("api_status"),

    /**
     * 邮箱服务器地址
     */
    MAIL_SMTP_HOST("mail_smtp_host"),

    /**
     * 邮箱地址
     */
    MAIL_SMTP_USERNAME("mail_smtp_username"),

    /**
     * 邮箱密码／授权码
     */
    MAIL_SMTP_PASSWORD("mail_smtp_password"),

    /**
     * 发送者名称
     */
    MAIL_FROM_NAME("mail_from_name"),

    /**
     * 启用邮件服务
     */
    SMTP_EMAIL_ENABLE("smtp_email_enable"),

    /**
     * 邮件回复通知
     */
    COMMENT_REPLY_NOTICE("comment_reply_notice"),

    /**
     * 新评论是否需要审核
     */
    NEW_COMMENT_NEED_CHECK("new_comment_need_check"),

    /**
     * 新评论通知
     */
    NEW_COMMENT_NOTICE("new_comment_notice"),

    /**
     * 邮件审核通过通知
     */
    COMMENT_PASS_NOTICE("comment_pass_notice"),

    /**
     * 站点描述
     */
    SEO_DESC("seo_desc"),

    /**
     * 博客主题
     */
    THEME("theme"),

    /**
     * 博客搭建日期
     */
    BLOG_START("blog_start"),

    /**
     * 博客评论系统
     */
    COMMENT_SYSTEM("comment_system"),

    /**
     * 仪表盘部件 文章总数
     */
    WIDGET_POSTCOUNT("widget_postcount"),

    /**
     * 仪表盘部件 评论总数
     */
    WIDGET_COMMENTCOUNT("widget_commentcount"),

    /**
     * 仪表盘部件 附件总数
     */
    WIDGET_ATTACHMENTCOUNT("widget_attachmentcount"),

    /**
     * 仪表盘部件 成立天数
     */
    WIDGET_DAYCOUNT("widget_daycount"),

    /**
     * 默认缩略图地址
     */
    DEFAULT_THUMBNAIL("/static/images/thumbnail/thumbnail.png");

    private String prop;

    BlogPropertiesEnum(String prop) {
        this.prop = prop;
    }

    public String getProp() {
        return prop;
    }
}
