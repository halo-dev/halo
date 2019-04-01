package cc.ryanc.halo.model.properties;

/**
 * SEO properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum SeoProperties implements PropertyEnum {

    KEYWORDS("seo_keywords", String.class),

    DESCRIPTION("seo_description", String.class),

    BAIDU_TOKEN("seo_baidu_token", String.class),

    VERIFICATION_BAIDU("seo_verification_baidu", String.class),

    VERIFICATION_GOOGLE("seo_verification_google", String.class),

    VERIFICATION_BING("seo_verification_bing", String.class),

    VERIFICATION_QIHU("seo_verification_qihu", String.class);

    private final String value;

    private final Class<?> type;

    SeoProperties(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }}
