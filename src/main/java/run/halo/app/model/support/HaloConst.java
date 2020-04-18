package run.halo.app.model.support;

import org.springframework.http.HttpHeaders;

import java.io.File;

/**
 * <pre>
 *     公共常量
 * </pre>
 *
 * @author ryanwang
 * @date 2017/12/29
 */
public class HaloConst {

    /**
     * User home directory.
     */
    public static final String USER_HOME = System.getProperties().getProperty("user.home");

    /**
     * Temporary directory.
     */
    public static final String TEMP_DIR = System.getProperties().getProperty("java.io.tmpdir");

    public static final String PROTOCOL_HTTPS = "https://";

    public static final String PROTOCOL_HTTP = "http://";

    public static final String URL_SEPARATOR = "/";

    /**
     * Halo backup prefix.
     */
    public static final String HALO_BACKUP_PREFIX = "halo-backup-";

    /**
     * Halo data export prefix.
     */
    public static final String HALO_DATA_EXPORT_PREFIX = "halo-data-export-";

    /**
     * Static pages pack prefix.
     */
    public static final String STATIC_PAGE_PACK_PREFIX = "static-pages-";

    /**
     * Default theme name.
     */
    public static final String DEFAULT_THEME_ID = "caicai_anatole";

    /**
     * Path separator.
     */
    public static final String FILE_SEPARATOR = File.separator;
    /**
     * Suffix of freemarker template file
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
    public static final String NETEASE_MUSIC_IFRAME = "<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=330 height=86 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=1&height=66\"></iframe>";
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
    public static final String BILIBILI_VIDEO_IFRAME = "<iframe height=$3 width=$2 src=\"//player.bilibili.com/player.html?aid=$1\" scrolling=\"no\" border=\"0\" frameborder=\"no\" framespacing=\"0\" allowfullscreen=\"true\"> </iframe>";
    /**
     * 哔哩哔哩视频正则表达式
     */
    public static final String BILIBILI_VIDEO_REG_PATTERN = "\\[bilibili:(\\d+)\\,(\\d+)\\,(\\d+)\\]";
    /**
     * YouTube 视频短代码前缀
     */
    public static final String YOUTUBE_VIDEO_PREFIX = "[youtube:";
    /**
     * YouTube 视频 iframe 代码
     */
    public static final String YOUTUBE_VIDEO_IFRAME = "<iframe width=$2 height=$3 src=\"https://www.youtube.com/embed/$1\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";
    /**
     * YouTube 视频正则表达式
     */
    public static final String YOUTUBE_VIDEO_REG_PATTERN = "\\[youtube:(\\w+)\\,(\\d+)\\,(\\d+)\\]";
    /**
     * Github Api url for halo-admin release.
     */
    public static final String HALO_ADMIN_RELEASES_LATEST = "https://api.github.com/repos/halo-dev/halo-admin/releases/latest";
    /**
     * Halo admin version regex.
     */
    public static final String HALO_ADMIN_VERSION_REGEX = "halo-admin-\\d+\\.\\d+(\\.\\d+)?(-\\S*)?\\.zip";
    public static final String HALO_ADMIN_RELATIVE_PATH = "templates/admin/";
    public static final String HALO_ADMIN_RELATIVE_BACKUP_PATH = "templates/admin-backup/";
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
     * Database product name.
     */
    public static String DATABASE_PRODUCT_NAME = null;
    /**
     * user_session
     */
    public static String USER_SESSION_KEY = "user_session";

    /**
     * Version constant.
     */
    public static String HALO_VERSION = null;

}
