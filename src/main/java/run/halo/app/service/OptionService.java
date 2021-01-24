package run.halo.app.service;

import com.qiniu.common.Zone;
import com.qiniu.storage.Region;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.exception.MissingPropertyException;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.dto.OptionSimpleDTO;
import run.halo.app.model.entity.Option;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.model.enums.ValueEnum;
import run.halo.app.model.params.OptionParam;
import run.halo.app.model.params.OptionQuery;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.service.base.CrudService;

/**
 * Option service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface OptionService extends CrudService<Option, Integer> {

    int DEFAULT_POST_PAGE_SIZE = 10;

    int DEFAULT_ARCHIVES_PAGE_SIZE = 10;

    int DEFAULT_COMMENT_PAGE_SIZE = 10;

    int DEFAULT_RSS_PAGE_SIZE = 20;

    String OPTIONS_KEY = "options";

    /**
     * Save multiple options
     *
     * @param options options
     */
    @Transactional
    void save(@Nullable Map<String, Object> options);

    /**
     * Save multiple options
     *
     * @param optionParams option params
     */
    @Transactional
    void save(@Nullable List<OptionParam> optionParams);

    /**
     * Save single option.
     *
     * @param optionParam option param
     */
    void save(@Nullable OptionParam optionParam);

    /**
     * Update option by id.
     *
     * @param optionId option id must not be null.
     * @param optionParam option param must not be null.
     */
    void update(@NonNull Integer optionId, @NonNull OptionParam optionParam);

    /**
     * Saves a property.
     *
     * @param property must not be null
     * @param value could be null
     */
    @Transactional
    void saveProperty(@NonNull PropertyEnum property, @Nullable String value);

    /**
     * Saves blog properties.
     *
     * @param properties blog properties
     */
    @Transactional
    void saveProperties(@NonNull Map<? extends PropertyEnum, String> properties);

    /**
     * Get all options
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
    Map<String, Object> listOptions(@Nullable List<String> keys);

    /**
     * Lists all option dtos.
     *
     * @return a list of option dto
     */
    @NonNull
    List<OptionDTO> listDtos();

    /**
     * Pages option output dtos.
     *
     * @param pageable page info must not be null
     * @param optionQuery optionQuery
     * @return a page of option output dto
     */
    Page<OptionSimpleDTO> pageDtosBy(@NonNull Pageable pageable, OptionQuery optionQuery);

    /**
     * Removes option permanently.
     *
     * @param id option id must not be null
     * @return option detail deleted
     */
    @NonNull
    Option removePermanently(@NonNull Integer id);

    /**
     * Get option by key
     *
     * @param key option key must not be blank
     * @return option value or null
     */
    @Nullable
    Object getByKeyOfNullable(@NonNull String key);

    /**
     * Gets option value of non null.
     *
     * @param key option key must not be null
     * @return option value of non null
     */
    @NonNull
    Object getByKeyOfNonNull(@NonNull String key);

    /**
     * Get option by key
     *
     * @param key option key must not be blank
     * @return an optional option value
     */
    @NonNull
    Optional<Object> getByKey(@NonNull String key);

    /**
     * Gets value by key.
     *
     * @param key key must not be null
     * @param valueType value type must not be null
     * @param <T> value type
     * @return value
     */
    @NonNull
    <T> Optional<T> getByKey(@NonNull String key, @NonNull Class<T> valueType);

    /**
     * Gets option value by blog property.
     *
     * @param property blog property must not be null
     * @return an option value
     */
    @Nullable
    Object getByPropertyOfNullable(@NonNull PropertyEnum property);

    /**
     * Gets option value by blog property.
     *
     * @param property blog property
     * @return an optiona value
     * @throws MissingPropertyException throws when property value dismisses
     */
    @NonNull
    Object getByPropertyOfNonNull(@NonNull PropertyEnum property);

    /**
     * Gets option value by blog property.
     *
     * @param property blog property must not be null
     * @return an optional option value
     */
    @NonNull
    Optional<Object> getByProperty(@NonNull PropertyEnum property);

    /**
     * Gets property value by blog property.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param <T> property type
     * @return property value
     */
    <T> Optional<T> getByProperty(@NonNull PropertyEnum property, @NonNull Class<T> propertyType);

    /**
     * Gets property value by blog property.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param defaultValue default value
     * @param <T> property type
     * @return property value
     */
    <T> T getByPropertyOrDefault(@NonNull PropertyEnum property, @NonNull Class<T> propertyType,
        T defaultValue);

    /**
     * Gets property value by blog property.
     * <p>
     * Default value from property default value.
     *
     * @param property blog property must not be null
     * @param propertyType property type must not be null
     * @param <T> property type
     * @return property value
     */
    <T> T getByPropertyOrDefault(@NonNull PropertyEnum property, @NonNull Class<T> propertyType);

    /**
     * Gets value by key.
     *
     * @param key key must not be null
     * @param valueType value type must not be null
     * @param defaultValue default value
     * @param <T> property type
     * @return value
     */
    <T> T getByKeyOrDefault(@NonNull String key, @NonNull Class<T> valueType, T defaultValue);

    /**
     * Gets enum value by property.
     *
     * @param property property must not be blank
     * @param valueType enum value type must not be null
     * @param <T> enum value type
     * @return an optional enum value
     */
    @NonNull
    <T extends Enum<T>> Optional<T> getEnumByProperty(@NonNull PropertyEnum property,
        @NonNull Class<T> valueType);

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
    <T extends Enum<T>> T getEnumByPropertyOrDefault(@NonNull PropertyEnum property,
        @NonNull Class<T> valueType, @Nullable T defaultValue);

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
    <V, E extends ValueEnum<V>> Optional<E> getValueEnumByProperty(@NonNull PropertyEnum property,
        @NonNull Class<V> valueType, @NonNull Class<E> enumType);

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
    <V, E extends ValueEnum<V>> E getValueEnumByPropertyOrDefault(@NonNull PropertyEnum property,
        @NonNull Class<V> valueType, @NonNull Class<E> enumType, @Nullable E defaultValue);

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
    Zone getQnYunZone();

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
    String getBlogTitle();

    /**
     * Gets global seo keywords.
     *
     * @return keywords
     */
    String getSeoKeywords();

    /**
     * Get global seo description.
     *
     * @return description
     */
    String getSeoDescription();

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
    PostPermalinkType getPostPermalinkType();

    /**
     * Get sheet permalink type.
     *
     * @return SheetPermalinkType
     */
    SheetPermalinkType getSheetPermalinkType();

    /**
     * Get sheet custom prefix.
     *
     * @return sheet prefix.
     */
    String getSheetPrefix();

    /**
     * Get links page custom prefix.
     *
     * @return links page prefix.
     */
    String getLinksPrefix();

    /**
     * Get photos page custom prefix.
     *
     * @return photos page prefix.
     */
    String getPhotosPrefix();

    /**
     * Get journals page custom prefix.
     *
     * @return journals page prefix.
     */
    String getJournalsPrefix();

    /**
     * Get archives custom prefix.
     *
     * @return archives prefix.
     */
    String getArchivesPrefix();

    /**
     * Get categories custom prefix.
     *
     * @return categories prefix.
     */
    String getCategoriesPrefix();

    /**
     * Get tags custom prefix.
     *
     * @return tags prefix.
     */
    String getTagsPrefix();

    /**
     * Get custom path suffix.
     *
     * @return path suffix.
     */
    String getPathSuffix();

    /**
     * Is enabled absolute path.
     *
     * @return true or false.
     */
    Boolean isEnabledAbsolutePath();

    /**
     * Replace option url in batch.
     *
     * @param oldUrl old blog url.
     * @param newUrl new blog url.
     * @return replaced options.
     */
    List<OptionDTO> replaceUrl(@NonNull String oldUrl, @NonNull String newUrl);

    /**
     * Converts to option output dto.
     *
     * @param option option must not be null
     * @return an option output dto
     */
    @NonNull
    OptionSimpleDTO convertToDto(@NonNull Option option);
}
