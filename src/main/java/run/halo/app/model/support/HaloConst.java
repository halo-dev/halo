package run.halo.app.model.support;

import java.io.File;
import java.util.Optional;
import org.springframework.http.HttpHeaders;

/**
 * Halo constants.
 *
 * @author ryanwang
 * @author guqing
 * @date 2017/12/29
 */
public class HaloConst {

    /**
     * User home directory.
     */
    public static final String USER_HOME = System.getProperty("user.home");

    /**
     * Temporary directory.
     */
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public static final String PROTOCOL_HTTPS = "https://";

    public static final String PROTOCOL_HTTP = "http://";

    public static final String URL_SEPARATOR = "/";

    /**
     * Halo backup prefix.
     */
    public static final String HALO_BACKUP_PREFIX = "halo-backup-";

    /**
     * Halo backup markdown prefix.
     */
    public static final String HALO_BACKUP_MARKDOWN_PREFIX = "halo-backup-markdown-";

    /**
     * Halo data export prefix.
     */
    public static final String HALO_DATA_EXPORT_PREFIX = "halo-data-export-";

    /**
     * Default theme name.
     */
    public static final String DEFAULT_THEME_ID = "caicai_anatole";

    /**
     * Default theme directory name.
     */
    public static final String DEFAULT_THEME_DIR_NAME = "anatole";

    /**
     * Default error path.
     */
    public static final String DEFAULT_ERROR_PATH = "common/error/error";

    /**
     * Default tag color.
     */
    public static final String DEFAULT_TAG_COLOR = "#cfd3d7";

    /**
     * Path separator.
     */
    public static final String FILE_SEPARATOR = File.separator;

    /**
     * Post password template name.
     */
    public static final String POST_PASSWORD_TEMPLATE = "post_password";

    /**
     * Suffix of freemarker template file.
     */
    public static final String SUFFIX_FTL = ".ftl";
    /**
     * Custom freemarker tag method key.
     */
    public static final String METHOD_KEY = "method";
    /**
     * 网易云音乐短代码前缀
     */
    public static final String NETEASE_MUSIC_PREFIX = "[music:";
    /**
     * 网易云音乐 iframe 代码
     */
    public static final String NETEASE_MUSIC_IFRAME =
        "<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=330 "
            + "height=86 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=1&height=66"
            + "\"></iframe>";
    /**
     * 网易云音乐短代码正则表达式
     */
    public static final String NETEASE_MUSIC_REG_PATTERN = "\\[music:(\\d+)\\]";
    /**
     * 哔哩哔哩视频短代码前缀
     */
    public static final String BILIBILI_VIDEO_PREFIX = "[bilibili:";
    /**
     * 哔哩哔哩视频 iframe 代码
     */
    public static final String BILIBILI_VIDEO_IFRAME =
        "<iframe height=$3 width=$2 src=\"//player.bilibili.com/player.html?aid=$1\" "
            + "scrolling=\"no\" border=\"0\" frameborder=\"no\" framespacing=\"0\" "
            + "allowfullscreen=\"true\"> </iframe>";
    /**
     * 哔哩哔哩视频正则表达式
     */
    public static final String BILIBILI_VIDEO_REG_PATTERN =
        "\\[bilibili:(\\d+)\\,(\\d+)\\,(\\d+)\\]";
    /**
     * YouTube 视频短代码前缀
     */
    public static final String YOUTUBE_VIDEO_PREFIX = "[youtube:";
    /**
     * YouTube 视频 iframe 代码
     */
    public static final String YOUTUBE_VIDEO_IFRAME =
        "<iframe width=$2 height=$3 src=\"https://www.youtube.com/embed/$1\" frameborder=\"0\" "
            + "allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" "
            + "allowfullscreen></iframe>";
    /**
     * YouTube 视频正则表达式
     */
    public static final String YOUTUBE_VIDEO_REG_PATTERN = "\\[youtube:(\\w+)\\,(\\d+)\\,(\\d+)\\]";
    /**
     * Content token header name.
     */
    public static final String API_ACCESS_KEY_HEADER_NAME = "API-" + HttpHeaders.AUTHORIZATION;
    /**
     * Admin token header name.
     */
    public static final String ADMIN_TOKEN_HEADER_NAME = "ADMIN-" + HttpHeaders.AUTHORIZATION;
    /**
     * Admin token param name.
     */
    public static final String ADMIN_TOKEN_QUERY_NAME = "admin_token";
    /**
     * Temporary token.
     */
    public static final String TEMP_TOKEN = "temp_token";
    /**
     * Content api token param name
     */
    public static final String API_ACCESS_KEY_QUERY_NAME = "api_access_key";
    public static final String ONE_TIME_TOKEN_QUERY_NAME = "ott";
    public static final String ONE_TIME_TOKEN_HEADER_NAME = "ott";
    /**
     * Version constant. (Available in production environment)
     */
    public static final String HALO_VERSION;

    /**
     * Unknown version: unknown
     */
    public static final String UNKNOWN_VERSION = "unknown";

    /**
     * Database product name.
     */
    public static String DATABASE_PRODUCT_NAME = null;

    /**
     * Options cache key.
     */
    public static final String OPTIONS_CACHE_KEY = "options";

    public static final String PRIVATE_OPTION_KEY = "private_options";

    static {
        // Set version
        HALO_VERSION = Optional.ofNullable(HaloConst.class.getPackage().getImplementationVersion())
            .orElse(UNKNOWN_VERSION);
    }
}
