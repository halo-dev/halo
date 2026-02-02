package run.halo.app.theme.finders.vo;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import lombok.Builder;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Site setting value object for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
@Builder
public record SiteSettingVo(

    String title,

    @With URL url,

    @With String version,

    String subtitle,

    String logo,

    String favicon,

    String language,

    Boolean allowRegistration,

    PostSetting post,

    SeoSetting seo,

    Routes routes,

    CommentSetting comment

) {

    /**
     * Convert to system {@link ConfigMap} to {@link SiteSettingVo}.
     *
     * @param data config map data
     * @return site setting value object
     */
    public static SiteSettingVo from(Map<String, String> data) {
        Assert.notNull(data, "Config data must not be null");
        SystemSetting.Basic basicSetting =
            toObject(data.get(SystemSetting.Basic.GROUP), SystemSetting.Basic.class);

        SystemSetting.User userSetting =
            toObject(data.get(SystemSetting.User.GROUP), SystemSetting.User.class);

        SystemSetting.Post postSetting =
            toObject(data.get(SystemSetting.Post.GROUP), SystemSetting.Post.class);

        SystemSetting.Seo seoSetting =
            toObject(data.get(SystemSetting.Seo.GROUP), SystemSetting.Seo.class);

        var routeRules = toObject(data.get(ThemeRouteRules.GROUP), ThemeRouteRules.class);

        SystemSetting.Comment commentSetting = toObject(data.get(SystemSetting.Comment.GROUP),
            SystemSetting.Comment.class);
        return builder()
            .title(basicSetting.getTitle())
            .subtitle(basicSetting.getSubtitle())
            .logo(basicSetting.getLogo())
            .favicon(basicSetting.getFavicon())
            .allowRegistration(userSetting.isAllowRegistration())
            .language(basicSetting.useSystemLocale().orElse(Locale.getDefault()).toLanguageTag())
            .post(PostSetting.builder()
                .postPageSize(postSetting.getPostPageSize())
                .archivePageSize(postSetting.getArchivePageSize())
                .categoryPageSize(postSetting.getCategoryPageSize())
                .tagPageSize(postSetting.getTagPageSize())
                .authorPageSize(postSetting.getAuthorPageSize())
                .build())
            .seo(SeoSetting.builder()
                .blockSpiders(seoSetting.getBlockSpiders())
                .keywords(seoSetting.getKeywords())
                .description(seoSetting.getDescription())
                .build())
            .comment(CommentSetting.builder()
                .enable(commentSetting.getEnable())
                .requireReviewForNew(commentSetting.getRequireReviewForNew())
                .systemUserOnly(commentSetting.getSystemUserOnly())
                .build())
            .routes(Routes.builder()
                .categoriesUri(StringUtils.prependIfMissing(routeRules.getCategories(), "/"))
                .tagsUri(StringUtils.prependIfMissing(routeRules.getTags(), "/"))
                .archivesUri(StringUtils.prependIfMissing(routeRules.getArchives(), "/"))
                .build()
            )
            .build();
    }

    private static <T> T toObject(String json, Class<T> type) {
        if (json == null) {
            // empty object
            json = "{}";
        }
        return JsonUtils.jsonToObject(json, type);
    }

    @Builder
    public record PostSetting(
        Integer postPageSize,
        Integer archivePageSize,
        Integer categoryPageSize,
        Integer tagPageSize,
        Integer authorPageSize
    ) {
    }

    @Builder
    public record SeoSetting(
        Boolean blockSpiders,
        String keywords,
        String description
    ) {
    }

    @Builder
    public record CommentSetting(
        Boolean enable,
        Boolean systemUserOnly,
        Boolean requireReviewForNew
    ) {
    }

    @Builder
    public record Routes(
        String categoriesUri,
        String tagsUri,
        String archivesUri
    ) {
    }
}
