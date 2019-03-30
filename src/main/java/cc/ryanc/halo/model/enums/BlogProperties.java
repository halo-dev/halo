package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
public enum BlogProperties implements PropertyEnum {

    /**
     * 博客语言
     */
    BLOG_LOCALE("blog_locale", String.class),

    /**
     * 博客标题
     */
    BLOG_TITLE("blog_title", String.class),

    /**
     * 博客地址
     */
    BLOG_URL("blog_url", String.class),

    /**
     * 文章摘要字数
     */
    POST_SUMMARY("post_summary", Long.class),

    /**
     * 首页文章条数
     */
    INDEX_POSTS("index_posts", Integer.class),

    /**
     * 每页评论条数
     */
    INDEX_COMMENTS("index_comments", Integer.class),

    /**
     * 是否已经安装
     */
    IS_INSTALL("is_install", Boolean.class),

    /**
     * RSS显示文章条数
     */
    RSS_POSTS("rss_posts", Integer.class),

    /**
     * API状态
     */
    API_STATUS("api_status", Boolean.class),

    /**
     * 邮箱服务器地址
     */
    MAIL_SMTP_HOST("mail_smtp_host", String.class),

    /**
     * 邮箱地址
     */
    MAIL_SMTP_USERNAME("mail_smtp_username", String.class),

    /**
     * 邮箱密码／授权码
     */
    MAIL_SMTP_PASSWORD("mail_smtp_password", String.class),

    /**
     * 发送者名称
     */
    MAIL_FROM_NAME("mail_from_name", String.class),

    /**
     * 启用邮件服务
     */
    SMTP_EMAIL_ENABLE("smtp_email_enable", Boolean.class),

    /**
     * 邮件回复通知
     */
    COMMENT_REPLY_NOTICE("comment_reply_notice", Boolean.class),

    /**
     * 新评论是否需要审核
     */
    NEW_COMMENT_NEED_CHECK("new_comment_need_check", Boolean.class),

    /**
     * 新评论通知
     */
    NEW_COMMENT_NOTICE("new_comment_notice", Boolean.class),

    /**
     * 邮件审核通过通知
     */
    COMMENT_PASS_NOTICE("comment_pass_notice", Boolean.class),

    /**
     * 站点描述
     */
    SEO_DESC("seo_desc", String.class),

    /**
     * 博客主题
     */
    THEME("theme", String.class),

    /**
     * 博客搭建日期
     */
    BLOG_START("blog_start", Long.class),

    /**
     * 仪表盘部件 文章总数
     */
    WIDGET_POSTCOUNT("widget_postcount", Boolean.class),

    /**
     * 仪表盘部件 评论总数
     */
    WIDGET_COMMENTCOUNT("widget_commentcount", Boolean.class),

    /**
     * 仪表盘部件 附件总数
     */
    WIDGET_ATTACHMENTCOUNT("widget_attachmentcount", Boolean.class),

    /**
     * 仪表盘部件 成立天数
     */
    WIDGET_DAYCOUNT("widget_daycount", Boolean.class),

    /**
     * API Token
     */
    API_TOKEN("api_token", String.class),

    /**
     * 附件存储位置
     */
    ATTACHMENT_TYPE("attachment_type", String.class);

    private String value;

    private Class<?> type;

    BlogProperties(String value, Class<?> type) {
        if (!PropertyEnum.isSupportedType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        this.value = value;
        this.type = type;
    }

    /**
     * Get enum value.
     *
     * @return enum value
     */
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

}
