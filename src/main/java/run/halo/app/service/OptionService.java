package run.halo.app.service;

import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.OptionSimpleDTO;
import run.halo.app.model.entity.Option;
import run.halo.app.model.params.OptionParam;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.service.base.CrudService;

/**
 * Option service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface OptionService extends CrudService<Option, Integer>, OptionProvideService {

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
     * Removes option permanently.
     *
     * @param id option id must not be null
     * @return option detail deleted
     */
    @NonNull
    Option removePermanently(@NonNull Integer id);

    /**
     * Converts to option output dto.
     *
     * @param option option must not be null
     * @return an option output dto
     */
    @NonNull
    OptionSimpleDTO convertToDto(@NonNull Option option);
}
