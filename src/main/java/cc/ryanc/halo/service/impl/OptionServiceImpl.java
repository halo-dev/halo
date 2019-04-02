package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.MissingPropertyException;
import cc.ryanc.halo.model.dto.OptionOutputDTO;
import cc.ryanc.halo.model.entity.Option;
import cc.ryanc.halo.model.enums.OptionSource;
import cc.ryanc.halo.model.enums.ValueEnum;
import cc.ryanc.halo.model.params.OptionParam;
import cc.ryanc.halo.model.properties.*;
import cc.ryanc.halo.repository.OptionRepository;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import com.qiniu.common.Zone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OptionService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer> implements OptionService {

    private final OptionRepository optionRepository;

    public OptionServiceImpl(OptionRepository optionRepository) {
        super(optionRepository);
        this.optionRepository = optionRepository;
    }

    /**
     * Saves one option
     *
     * @param key    key
     * @param value  value
     * @param source source
     */
    @Override
    public void save(String key, String value, OptionSource source) {
        Assert.hasText(key, "Option key must not be blank");

        if (StringUtils.isBlank(value)) {
            // If the value is blank, remove the key
            optionRepository.removeByOptionKey(key);
            return;
        }

        // TODO Consider cache options with map
        Option option = optionRepository.findByOptionKey(key).map(anOption -> {
            // Exist
            anOption.setOptionValue(value);
            return anOption;
        }).orElseGet(() -> {
            // Not exist
            Option anOption = new Option();
            anOption.setOptionKey(key);
            anOption.setOptionValue(value);
            anOption.setSource(source);
            return anOption;
        });

        // Save or update the options
        optionRepository.save(option);
    }

    /**
     * Saves multiple options
     *
     * @param options options
     * @param source  source
     */
    @Override
    public void save(Map<String, String> options, OptionSource source) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }

        // TODO Optimize the queries
        options.forEach((key, value) -> save(key, value, source));
    }

    @Override
    public void save(List<OptionParam> optionParams, OptionSource source) {
        if (CollectionUtils.isEmpty(optionParams)) {
            return;
        }

        // TODO Optimize the query
        optionParams.forEach(optionParam -> save(optionParam.getOptionKey(), optionParam.getOptionValue(), source));
    }

    @Override
    public void saveProperties(Map<? extends PropertyEnum, String> properties, OptionSource source) {
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }

        properties.forEach((property, value) -> save(property.getValue(), value, source));
    }

    /**
     * Gets all options
     *
     * @return Map
     */
    @Override
    public Map<String, String> listOptions() {
        return ServiceUtils.convertToMap(listAll(), Option::getOptionKey, Option::getOptionValue);
    }

    @Override
    public List<OptionOutputDTO> listDtos() {
        return listAll().stream().map(option -> new OptionOutputDTO().<OptionOutputDTO>convertFrom(option)).collect(Collectors.toList());
    }

    /**
     * Gets option by key
     *
     * @param key key
     * @return String
     */
    @Override
    public String getByKeyOfNullable(String key) {
        return getByKey(key).orElse(null);
    }

    @Override
    public String getByKeyOfNonNull(String key) {
        return getByKey(key).orElseThrow(() -> new MissingPropertyException("You have to config " + key + " setting"));
    }

    @Override
    public Optional<String> getByKey(String key) {
        Assert.hasText(key, "Option key must not be blank");

        return optionRepository.findByOptionKey(key).map(Option::getOptionValue);
    }

    @Override
    public String getByPropertyOfNullable(PropertyEnum property) {
        return getByProperty(property).orElse(null);
    }

    @Override
    public String getByPropertyOfNonNull(PropertyEnum property) {
        Assert.notNull(property, "Blog property must not be null");

        return getByKeyOfNonNull(property.getValue());
    }

    @Override
    public Optional<String> getByProperty(PropertyEnum property) {
        Assert.notNull(property, "Blog property must not be null");

        return getByKey(property.getValue());
    }

    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType, T defaultValue) {
        Assert.notNull(property, "Blog property must not be null");

        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> getByProperty(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property).map(propertyValue -> PropertyEnum.convertTo(propertyValue, propertyType));
    }

    @Override
    public <T> T getByKeyOrDefault(String key, Class<T> valueType, T defaultValue) {
        return getByKey(key, valueType).orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> getByKey(String key, Class<T> valueType) {
        return getByKey(key).map(value -> PropertyEnum.convertTo(value, valueType));
    }

    @Override
    public <T extends Enum<T>> Optional<T> getEnumByProperty(PropertyEnum property, Class<T> valueType) {
        return getByProperty(property).map(value -> PropertyEnum.convertToEnum(value, valueType));
    }

    @Override
    public <T extends Enum<T>> T getEnumByPropertyOrDefault(PropertyEnum property, Class<T> valueType, T defaultValue) {
        return getEnumByProperty(property, valueType).orElse(defaultValue);
    }

    @Override
    public <V, E extends ValueEnum<V>> Optional<E> getValueEnumByProperty(PropertyEnum property, Class<V> valueType, Class<E> enumType) {
        return getByProperty(property).map(value -> ValueEnum.valueToEnum(enumType, PropertyEnum.convertTo(value, valueType)));
    }

    @Override
    public <V, E extends ValueEnum<V>> E getValueEnumByPropertyOrDefault(PropertyEnum property, Class<V> valueType, Class<E> enumType, E defaultValue) {
        return getValueEnumByProperty(property, valueType, enumType).orElse(defaultValue);
    }

    @Override
    public int getPostPageSize() {
        try {
            return getByPropertyOrDefault(PostProperties.INDEX_PAGE_SIZE, Integer.class, DEFAULT_COMMENT_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(PostProperties.INDEX_PAGE_SIZE.getValue() + " option is not a number format", e);
            return DEFAULT_POST_PAGE_SIZE;
        }
    }

    @Override
    public int getCommentPageSize() {
        try {
            return getByPropertyOrDefault(CommentProperties.PAGE_SIZE, Integer.class, DEFAULT_COMMENT_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(CommentProperties.PAGE_SIZE.getValue() + " option is not a number format", e);
            return DEFAULT_COMMENT_PAGE_SIZE;
        }
    }

    @Override
    public int getRssPageSize() {
        try {
            return getByPropertyOrDefault(PostProperties.RSS_PAGE_SIZE, Integer.class, DEFAULT_COMMENT_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(PostProperties.RSS_PAGE_SIZE.getValue() + " setting is not a number format", e);
            return DEFAULT_RSS_PAGE_SIZE;
        }
    }

    @Override
    public Zone getQnYunZone() {
        return getByProperty(QnYunProperties.ZONE).map(qiniuZone -> {

            Zone zone;
            switch (qiniuZone) {
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

    @Override
    public Locale getLocale() {
        return getByProperty(BlogProperties.BLOG_LOCALE).map(localeStr -> {
            try {
                return Locale.forLanguageTag(localeStr);
            } catch (Exception e) {
                return Locale.getDefault();
            }
        }).orElseGet(Locale::getDefault);
    }

}
