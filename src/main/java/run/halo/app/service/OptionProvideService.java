package run.halo.app.service;

import com.qiniu.common.Zone;
import com.qiniu.storage.Region;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.MissingPropertyException;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.dto.OptionSimpleDTO;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.model.enums.ValueEnum;
import run.halo.app.model.params.OptionQuery;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.OtherProperties;
import run.halo.app.model.properties.PermalinkProperties;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.model.properties.QiniuOssProperties;
import run.halo.app.model.properties.SeoProperties;


/**
 * Option parameter provision service.
 *
 * @author LIlGG
 * @date 2021/8/2
 */
public interface OptionProvideService {
    /**
     * Get all options.
     *
     * @return Map
     */
    @NonNull
    @Transactional
    Map<String, Object> listOptions();

    /**
     * Lists options by key list.
     *
     * @param keys key list
     * @return a map of option
     */
    @NonNull
    default Map<String, Object> listOptions(@Nullable Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        Map<String, Object> optionMap = listOptions();

        Map<String, Object> result = new HashMap<>(keys.size());

        keys.stream()
            .filter(optionMap::containsKey)
            .forEach(key -> result.put(key, optionMap.get(key)));

        return result;
    }

    /**
     * Lists all option dtos.
     *
     * @return a list of option dto
     */
    @NonNull
    default List<OptionDTO> listDtos() {
        List<OptionDTO> result = new LinkedList<>();

        listOptions().forEach((key, value) -> result.add(new OptionDTO(key, value)));

        return result;
    }

    /**
     * Pages option output dtos.
     *
     * @param pageable page info must not be null
     * @param optionQuery optionQuery
     * @return a page of option output dto
     */
    Page<OptionSimpleDTO> pageDtosBy(@NonNull Pageable pageable, OptionQuery optionQuery);

    /**
     * Get option by key.
     *
     * @param key option key must not be blank
     * @return option value or null
     */
    @Nullable
    default Object getByKeyOfNullable(@NonNull String key) {
        return getByKey(key).orElse(null);
    }

    /**
     * Gets option value of non null.
     *
     * @param key option key must not be null
     * @return option value of non null
     */
    @NonNull
    default Object getByKeyOfNonNull(@NonNull String key) {
        return getByKey(key).orElseThrow(
            () -> new MissingPropertyException("You have to config " + key + " setting"));
    }

    /**
     * Get option by key.
     *
     * @param key option key must not be blank
     * @return an optional option value
     */
    @NonNull
    default Optional<Object> getByKey(@NonNull String key) {
        Assert.hasText(key, "Option key must not be blank");

        return Optional.ofNullable(listOptions().get(key));
    }

    /**
     * Gets value by key.
     *
     * @param key key must not be null
     * @param valueType value type must not be null
     * @param <T> value type
     * @return value
     */
    @NonNull
    default  <T> Optional<T> getByKey(@NonNull String key, @NonNull Class<T> valueType) {
        return getByKey(key).map(value -> PropertyEnum.convertTo(value.toString(), valueType));
    }

    /**
     * Gets option value by blog property.
     *
     * @param property blog property must not be null
     * @return an option value
     */
    @Nullable
    default Object getByPropertyOfNullable(@NonNull PropertyEnum property) {
        return getByProperty(property).orElse(null);
    }

    /**
     * Gets option value by blog property.
     *
     * @param property blog property
     * @return an optiona value
     * @throws MissingPropertyException throws when property value dismisses
     */
    @NonNull
    default Object getByPropertyOfNonNull(@NonNull PropertyEnum property) {
        Assert.notNull(property, "Blog property must not be null");

        return getByKeyOfNonNull(property.getValue());
    }

    /**
     * Gets option value by blog property.
     *
     * @param property blog property must not be null
     * @return an optional option value
     */
    @NonNull
    default Optional<Object> getByProperty(@NonNull PropertyEnum property) {
        Assert.notNull(property, "Blog property must not be null");

        return getByKey(property.getValue());
    }

    /**
     * Gets property value by blog property.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param <T> property type
     * @return property value
     */
    default <T> Optional<T> getByProperty(@NonNull PropertyEnum property,
        @NonNull Class<T> propertyType) {
        return getByProperty(property)
            .map(propertyValue -> PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }

    /**
     * Gets property value by blog property.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param defaultValue default value
     * @param <T> property type
     * @return property value
     */
    default <T> T getByPropertyOrDefault(@NonNull PropertyEnum property,
        @NonNull Class<T> propertyType,
        T defaultValue) {
        Assert.notNull(property, "Blog property must not be null");

        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    /**
     * Gets property value by blog property.
     *
     * Default value from property default value.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param <T> property type
     * @return property value
     */
    default  <T> T getByPropertyOrDefault(@NonNull PropertyEnum property,
        @NonNull Class<T> propertyType) {
        return getByProperty(property, propertyType).orElse(property.defaultValue(propertyType));
    }

    /**
     * Gets value by key.
     *
     * @param key key must not be null
     * @param valueType value type must not be null
     * @param defaultValue default value
     * @param <T> property type
     * @return value
     */
    default <T> T getByKeyOrDefault(@NonNull String key, @NonNull Class<T> valueType,
        T defaultValue) {
        return getByKey(key, valueType).orElse(defaultValue);
    }

    /**
     * Gets enum value by property.
     *
     * @param property property must not be blank
     * @param valueType enum value type must not be null
     * @param <T> enum value type
     * @return an optional enum value
     */
    @NonNull
    default <T extends Enum<T>> Optional<T> getEnumByProperty(@NonNull PropertyEnum property,
        @NonNull Class<T> valueType) {
        return getByProperty(property)
            .map(value -> PropertyEnum.convertToEnum(value.toString(), valueType));
    }

    /**
     * Gets enum value by property.
     *
     * @param property property must not be blank
     * @param valueType enum value type must not be null
     * @param defaultValue default value
     * @param <T> enum value type
     * @return enum value
     */
    @Nullable
    default <T extends Enum<T>> T getEnumByPropertyOrDefault(@NonNull PropertyEnum property,
        @NonNull Class<T> valueType, @Nullable T defaultValue) {
        return getEnumByProperty(property, valueType).orElse(defaultValue);
    }

    /**
     * Gets value enum by property.
     *
     * @param property property must not be blank
     * @param valueType enum value type must not be null
     * @param enumType enum type must not be null
     * @param <V> enum value type
     * @param <E> value enum type
     * @return an optional value enum value
     */
    @NonNull
    default <V, E extends Enum<E> & ValueEnum<V>> Optional<E> getValueEnumByProperty(
        @NonNull PropertyEnum property,
        @NonNull Class<V> valueType, @NonNull Class<E> enumType) {
        return getByProperty(property).map(value -> ValueEnum
            .valueToEnum(enumType, PropertyEnum.convertTo(value.toString(), valueType)));
    }

    /**
     * Gets value enum by property.
     *
     * @param property property must not be blank
     * @param valueType enum value type must not be null
     * @param enumType enum type must not be null
     * @param defaultValue default value enum value
     * @param <V> enum value type
     * @param <E> value enum type
     * @return value enum value or null if the default value is null
     */
    @Nullable
    default <V, E extends Enum<E> & ValueEnum<V>> E getValueEnumByPropertyOrDefault(
        @NonNull PropertyEnum property,
        @NonNull Class<V> valueType, @NonNull Class<E> enumType, @Nullable E defaultValue) {
        return getValueEnumByProperty(property, valueType, enumType).orElse(defaultValue);
    }

    /**
     * Gets post page size.
     *
     * @return page size
     */
    int getPostPageSize();

    /**
     * Gets archives page size.
     *
     * @return page size
     */
    int getArchivesPageSize();

    /**
     * Gets comment page size.
     *
     * @return page size
     */
    int getCommentPageSize();

    /**
     * Gets rss page size.
     *
     * @return page size
     */
    int getRssPageSize();

    /**
     * Get qi niu yun zone.
     *
     * @return qiniu zone
     */
    @NonNull
    @Deprecated
    default Zone getQnYunZone() {
        return getByProperty(QiniuOssProperties.OSS_ZONE).map(qiniuZone -> {

            Zone zone;
            switch (qiniuZone.toString()) {
                case "z0":
                    zone = Zone.zone0();
                    break;
                case "z1":
                    zone = Zone.zone1();
                    break;
                case "z2":
                    zone = Zone.zone2();
                    break;
                case "na0":
                    zone = Zone.zoneNa0();
                    break;
                case "as0":
                    zone = Zone.zoneAs0();
                    break;
                default:
                    // Default is detecting zone automatically
                    zone = Zone.autoZone();
            }
            return zone;

        }).orElseGet(Zone::autoZone);
    }

    /**
     * Get qiniu oss region.
     *
     * @return qiniu region
     */
    @NonNull
    Region getQiniuRegion();

    /**
     * Gets locale.
     *
     * @return locale user set or default locale
     */
    @NonNull
    Locale getLocale();

    /**
     * Gets blog base url. (Without /)
     *
     * @return blog base url (If blog url isn't present, current machine IP address will be default)
     */
    @NonNull
    String getBlogBaseUrl();

    /**
     * Gets blog title.
     *
     * @return blog title.
     */
    @NonNull
    default String getBlogTitle() {
        return getByProperty(BlogProperties.BLOG_TITLE).orElse("").toString();
    }

    /**
     * Gets global seo keywords.
     *
     * @return keywords
     */
    default String getSeoKeywords() {
        return getByProperty(SeoProperties.KEYWORDS).orElse("").toString();
    }

    /**
     * Get global seo description.
     *
     * @return description
     */
    default String getSeoDescription() {
        return getByProperty(SeoProperties.DESCRIPTION).orElse("").toString();
    }

    /**
     * Gets blog birthday.
     *
     * @return birthday timestamp
     */
    long getBirthday();

    /**
     * Get post permalink type.
     *
     * @return PostPermalinkType
     */
    default PostPermalinkType getPostPermalinkType() {
        return getEnumByPropertyOrDefault(PermalinkProperties.POST_PERMALINK_TYPE,
            PostPermalinkType.class, PostPermalinkType.DEFAULT);
    }

    /**
     * Get sheet permalink type.
     *
     * @return SheetPermalinkType
     */
    default SheetPermalinkType getSheetPermalinkType() {
        return getEnumByPropertyOrDefault(PermalinkProperties.SHEET_PERMALINK_TYPE,
            SheetPermalinkType.class, SheetPermalinkType.SECONDARY);
    }

    /**
     * Get sheet custom prefix.
     *
     * @return sheet prefix.
     */
    default String getSheetPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.SHEET_PREFIX, String.class,
            PermalinkProperties.SHEET_PREFIX.defaultValue());
    }

    /**
     * Get links page custom prefix.
     *
     * @return links page prefix.
     */
    default String getLinksPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.LINKS_PREFIX, String.class,
            PermalinkProperties.LINKS_PREFIX.defaultValue());
    }

    /**
     * Get photos page custom prefix.
     *
     * @return photos page prefix.
     */
    default String getPhotosPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.PHOTOS_PREFIX, String.class,
            PermalinkProperties.PHOTOS_PREFIX.defaultValue());
    }

    /**
     * Get journals page custom prefix.
     *
     * @return journals page prefix.
     */
    default String getJournalsPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.JOURNALS_PREFIX, String.class,
            PermalinkProperties.JOURNALS_PREFIX.defaultValue());
    }

    /**
     * Get archives custom prefix.
     *
     * @return archives prefix.
     */
    default String getArchivesPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class,
            PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
    }

    /**
     * Get categories custom prefix.
     *
     * @return categories prefix.
     */
    default String getCategoriesPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.CATEGORIES_PREFIX, String.class,
            PermalinkProperties.CATEGORIES_PREFIX.defaultValue());
    }

    /**
     * Get tags custom prefix.
     *
     * @return tags prefix.
     */
    default String getTagsPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.TAGS_PREFIX, String.class,
            PermalinkProperties.TAGS_PREFIX.defaultValue());
    }

    /**
     * Get custom path suffix.
     *
     * @return path suffix.
     */
    default String getPathSuffix() {
        return getByPropertyOrDefault(PermalinkProperties.PATH_SUFFIX, String.class,
            PermalinkProperties.PATH_SUFFIX.defaultValue());
    }

    default Boolean isEnabledAbsolutePath() {
        return getByPropertyOrDefault(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.class,
            true);
    }
}
