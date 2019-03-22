package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.OptionOutputDTO;
import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.params.OptionParam;
import cc.ryanc.halo.service.base.CrudService;
import com.qiniu.common.Zone;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Option service.
 *
 * @author johnniang
 */
public interface OptionService extends CrudService<Option, Integer> {

    int DEFAULT_POST_PAGE_SIZE = 10;

    int DEFAULT_COMMENT_PAGE_SIZE = 10;

    /**
     * Save one option
     *
     * @param key    key must not be blank
     * @param value  value
     * @param source source
     */
    void save(@NonNull String key, String value, String source);

    /**
     * Save multiple options
     *
     * @param options options
     * @param source  source
     */
    void save(@NonNull Map<String, String> options, String source);

    /**
     * SAve multiple options
     *
     * @param optionParams option params
     * @param source       source
     */
    void save(List<OptionParam> optionParams, String source);

    /**
     * Saves blog properties.
     *
     * @param properties blog properties
     * @param source     source
     */
    void saveProperties(@NonNull Map<BlogProperties, String> properties, String source);

    /**
     * Get all options
     *
     * @return Map
     */
    Map<String, String> listOptions();

    /**
     * Lists all option dtos.
     *
     * @return a list of option dto
     */
    List<OptionOutputDTO> listDtos();

    /**
     * Get option by key
     *
     * @param key option key must not be blank
     * @return option value or null
     */
    @Nullable
    String getByKeyOfNullable(@NonNull String key);

    /**
     * Get option by key
     *
     * @param key option key must not be blank
     * @return an optional option value
     */
    @NonNull
    Optional<String> getByKey(@NonNull String key);

    /**
     * Gets option by blog property.
     *
     * @param property blog property must not be null
     * @return an option value
     */
    @Nullable
    String getByPropertyOfNullable(@NonNull BlogProperties property);

    /**
     * Gets option by blog property.
     *
     * @param property blog property must not be null
     * @return an optional option value
     */
    @NonNull
    Optional<String> getByProperty(@NonNull BlogProperties property);

    /**
     * Gets post page size.
     *
     * @return page size
     */
    int getPostPageSize();

    /**
     * Gets comment page size.
     *
     * @return page size
     */
    int getCommentPageSize();

    /**
     * Get quniu zone.
     *
     * @return qiniu zone
     */
    @NonNull
    Zone getQiniuZone();

}
