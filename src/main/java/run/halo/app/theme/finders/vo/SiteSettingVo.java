package run.halo.app.theme.finders.vo;

import java.net.URI;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.util.Assert;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Site setting value object for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class SiteSettingVo {

    String title;

    @With
    URI url;

    String subtitle;

    String logo;

    String favicon;

    Boolean allowRegistration;

    PostSetting post;

    SeoSetting seo;

    CommentSetting comment;

    /**
     * Convert to system {@link ConfigMap} to {@link SiteSettingVo}.
     *
     * @param configMap config map named system
     * @return site setting value object
     */
    public static SiteSettingVo from(ConfigMap configMap) {
        Assert.notNull(configMap, "The configMap must not be null.");
        Map<String, String> data = configMap.getData();
        if (data == null) {
            return builder().build();
        }
        SystemSetting.Basic basicSetting =
            toObject(data.get(SystemSetting.Basic.GROUP), SystemSetting.Basic.class);

        SystemSetting.User userSetting =
            toObject(data.get(SystemSetting.User.GROUP), SystemSetting.User.class);

        SystemSetting.Post postSetting =
            toObject(data.get(SystemSetting.Post.GROUP), SystemSetting.Post.class);

        SystemSetting.Seo seoSetting =
            toObject(data.get(SystemSetting.Seo.GROUP), SystemSetting.Seo.class);

        SystemSetting.Comment commentSetting = toObject(data.get(SystemSetting.Comment.GROUP),
            SystemSetting.Comment.class);
        return builder()
            .title(basicSetting.getTitle())
            .subtitle(basicSetting.getSubtitle())
            .logo(basicSetting.getLogo())
            .favicon(basicSetting.getFavicon())
            .allowRegistration(userSetting.getAllowRegistration())
            .post(PostSetting.builder()
                .postPageSize(postSetting.getPostPageSize())
                .archivePageSize(postSetting.getArchivePageSize())
                .categoryPageSize(postSetting.getCategoryPageSize())
                .tagPageSize(postSetting.getTagPageSize())
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
            .build();
    }

    private static <T> T toObject(String json, Class<T> type) {
        if (json == null) {
            // empty object
            json = "{}";
        }
        return JsonUtils.jsonToObject(json, type);
    }

    @Value
    @Builder
    public static class PostSetting {
        Integer postPageSize;

        Integer archivePageSize;

        Integer categoryPageSize;

        Integer tagPageSize;
    }

    @Value
    @Builder
    public static class SeoSetting {
        Boolean blockSpiders;

        String keywords;

        String description;
    }

    @Value
    @Builder
    public static class CommentSetting {
        Boolean enable;

        Boolean systemUserOnly;

        Boolean requireReviewForNew;
    }
}
