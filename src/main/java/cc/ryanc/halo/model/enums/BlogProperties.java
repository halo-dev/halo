package cc.ryanc.halo.model.enums;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
public enum BlogProperties implements ValueEnum<String> {

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
     * 默认缩略图地址
     */
    DEFAULT_THUMBNAIL("/static/halo-content/images/thumbnail/thumbnail.png", String.class),

    /**
     * 自动备份
     */
    AUTO_BACKUP("auto_backup", Boolean.class),

    /**
     * API Token
     */
    API_TOKEN("api_token", String.class),

    /**
     * 附件存储位置
     */
    ATTACH_LOC("attach_loc", String.class),

    /**
     * 七牛云 Zone.
     */
    QINIU_ZONE("qiniu_zone", String.class);


    private String value;

    private Class<?> type;

    BlogProperties(String value, Class<?> type) {
        if (!supportType(type)) {
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

    public Class<?> getType() {
        return type;
    }

    /**
     * Converts to value with corresponding type
     *
     * @param value string value must not be null
     * @param type  property value type must not be null
     * @param <T>   property value type
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertTo(@NonNull String value, @NonNull Class<T> type) {
        Assert.hasText(value, "Property value must not be blank");

        if (!supportType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        }

        if (type.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        }

        if (type.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(value);
        }

        if (type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        }

        if (type.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(value);
        }

        if (type.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(value);
        }

        if (type.isAssignableFrom(Double.class)) {
            return (T) Byte.valueOf(value);
        }

        if (type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        }

        // Should never happen
        throw new UnsupportedOperationException("Unsupported blog property type:" + type.getName() + " provided");
    }

    /**
     * Check the type is support by the blog property.
     *
     * @param type type to check
     * @return true if supports; false else
     */
    public static boolean supportType(Class<?> type) {
        return type != null && (
                type.isAssignableFrom(String.class)
                        || type.isAssignableFrom(Number.class)
                        || type.isAssignableFrom(Integer.class)
                        || type.isAssignableFrom(Long.class)
                        || type.isAssignableFrom(Boolean.class)
                        || type.isAssignableFrom(Short.class)
                        || type.isAssignableFrom(Byte.class)
                        || type.isAssignableFrom(Double.class)
                        || type.isAssignableFrom(Float.class)
        );
    }

}
