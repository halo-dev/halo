package run.halo.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qiniu.common.Zone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.exception.MissingPropertyException;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.entity.Option;
import run.halo.app.model.enums.ValueEnum;
import run.halo.app.model.params.OptionParam;
import run.halo.app.model.properties.*;
import run.halo.app.repository.OptionRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.*;

/**
 * OptionService implementation class
 *
 * @author ryanwang
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer> implements OptionService {

    private final OptionRepository optionRepository;

    private final ApplicationContext applicationContext;

    private final StringCacheStore cacheStore;

    private final Map<String, PropertyEnum> propertyEnumMap;

    private final ApplicationEventPublisher eventPublisher;

    public OptionServiceImpl(OptionRepository optionRepository,
                             ApplicationContext applicationContext,
                             StringCacheStore cacheStore,
                             ApplicationEventPublisher eventPublisher) {
        super(optionRepository);
        this.optionRepository = optionRepository;
        this.applicationContext = applicationContext;
        this.cacheStore = cacheStore;
        this.eventPublisher = eventPublisher;

        propertyEnumMap = Collections.unmodifiableMap(PropertyEnum.getValuePropertyEnumMap());
    }

    private void save(String key, String value) {
        Assert.hasText(key, "Option key must not be blank");

        if (StringUtils.isBlank(value)) {
            // If the value is blank, remove the key
            optionRepository.deleteByKey(key);
            log.debug("Removed option key: [{}]", key);
            return;
        }

        Option option = optionRepository.findByKey(key)
                .map(anOption -> {
                    log.debug("Updating option key: [{}], value: from [{}] to [{}]", key, anOption.getValue(), value);
                    // Exist
                    anOption.setValue(value);
                    return anOption;
                }).orElseGet(() -> {
                    log.debug("Creating option key: [{}], value: [{}]", key, value);
                    // Not exist
                    Option anOption = new Option();
                    anOption.setKey(key);
                    anOption.setValue(value);
                    return anOption;
                });

        // Save or update the options
        Option savedOption = optionRepository.save(option);

        log.debug("Saved option: [{}]", savedOption);
    }

    @Override
    public void save(Map<String, String> options) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }

        options.forEach(this::save);

        publishOptionUpdatedEvent();
    }

    @Override
    public void save(List<OptionParam> optionParams) {
        if (CollectionUtils.isEmpty(optionParams)) {
            return;
        }

        optionParams.forEach(optionParam -> save(optionParam.getKey(), optionParam.getValue()));

        publishOptionUpdatedEvent();
    }

    @Override
    public void saveProperty(PropertyEnum property, String value) {
        Assert.notNull(property, "Property must not be null");

        save(property.getValue(), value);

        publishOptionUpdatedEvent();
    }

    @Override
    public void saveProperties(Map<? extends PropertyEnum, String> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }

        properties.forEach((property, value) -> save(property.getValue(), value));

        publishOptionUpdatedEvent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> listOptions() {
        // Get options from cache
        return cacheStore.getAny(OPTIONS_KEY, Map.class).orElseGet(() -> {
            List<Option> options = listAll();

            Set<String> keys = ServiceUtils.fetchProperty(options, Option::getKey);

            Map<String, Object> userDefinedOptionMap = ServiceUtils.convertToMap(options, Option::getKey, option -> {
                String key = option.getKey();

                PropertyEnum propertyEnum = propertyEnumMap.get(key);

                if (propertyEnum == null) {
                    return option.getValue();
                }

                return PropertyEnum.convertTo(option.getValue(), propertyEnum);
            });

            Map<String, Object> result = new HashMap<>(userDefinedOptionMap);

            // Add default property
            propertyEnumMap.keySet()
                    .stream()
                    .filter(key -> !keys.contains(key))
                    .forEach(key -> {
                        PropertyEnum propertyEnum = propertyEnumMap.get(key);

                        if (StringUtils.isBlank(propertyEnum.defaultValue())) {
                            return;
                        }

                        result.put(key, PropertyEnum.convertTo(propertyEnum.defaultValue(), propertyEnum));
                    });

            // Cache the result
            cacheStore.putAny(OPTIONS_KEY, result);

            return result;
        });
    }

    @Override
    public Map<String, Object> listOptions(List<String> keys) {
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

    @Override
    public List<OptionDTO> listDtos() {
        List<OptionDTO> result = new LinkedList<>();

        listOptions().forEach((key, value) -> result.add(new OptionDTO(key, value)));

        return result;
    }

    @Override
    public Object getByKeyOfNullable(String key) {
        return getByKey(key).orElse(null);
    }

    @Override
    public Object getByKeyOfNonNull(String key) {
        return getByKey(key).orElseThrow(() -> new MissingPropertyException("You have to config " + key + " setting"));
    }

    @Override
    public Optional<Object> getByKey(String key) {
        Assert.hasText(key, "Option key must not be blank");

        return Optional.ofNullable(listOptions().get(key));
    }

    @Override
    public Object getByPropertyOfNullable(PropertyEnum property) {
        return getByProperty(property).orElse(null);
    }

    @Override
    public Object getByPropertyOfNonNull(PropertyEnum property) {
        Assert.notNull(property, "Blog property must not be null");

        return getByKeyOfNonNull(property.getValue());
    }

    @Override
    public Optional<Object> getByProperty(PropertyEnum property) {
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
        return getByProperty(property).map(propertyValue -> PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }

    @Override
    public <T> T getByKeyOrDefault(String key, Class<T> valueType, T defaultValue) {
        return getByKey(key, valueType).orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> getByKey(String key, Class<T> valueType) {
        return getByKey(key).map(value -> PropertyEnum.convertTo(value.toString(), valueType));
    }

    @Override
    public <T extends Enum<T>> Optional<T> getEnumByProperty(PropertyEnum property, Class<T> valueType) {
        return getByProperty(property).map(value -> PropertyEnum.convertToEnum(value.toString(), valueType));
    }

    @Override
    public <T extends Enum<T>> T getEnumByPropertyOrDefault(PropertyEnum property, Class<T> valueType, T defaultValue) {
        return getEnumByProperty(property, valueType).orElse(defaultValue);
    }

    @Override
    public <V, E extends ValueEnum<V>> Optional<E> getValueEnumByProperty(PropertyEnum property, Class<V> valueType, Class<E> enumType) {
        return getByProperty(property).map(value -> ValueEnum.valueToEnum(enumType, PropertyEnum.convertTo(value.toString(), valueType)));
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

    @Override
    public Locale getLocale() {
        return getByProperty(BlogProperties.BLOG_LOCALE).map(localeStr -> {
            try {
                return Locale.forLanguageTag(localeStr.toString());
            } catch (Exception e) {
                return Locale.getDefault();
            }
        }).orElseGet(Locale::getDefault);
    }

    @Override
    public String getBlogBaseUrl() {
        // Get server port
        String serverPort = applicationContext.getEnvironment().getProperty("server.port", "8080");

        String blogUrl = getByProperty(BlogProperties.BLOG_URL).orElse("").toString();

        if (StrUtil.isNotBlank(blogUrl)) {
            blogUrl = StrUtil.removeSuffix(blogUrl, "/");
        } else {
            blogUrl = String.format("http://%s:%s", HaloUtils.getMachineIP(), serverPort);
        }

        return blogUrl;
    }

    @Override
    public String getBlogTitle() {
        return getByProperty(BlogProperties.BLOG_TITLE).orElse("").toString();
    }

    @Override
    public long getBirthday() {
        return getByProperty(PrimaryProperties.BIRTHDAY, Long.class).orElseGet(() -> {
            long currentTime = DateUtils.now().getTime();
            saveProperty(PrimaryProperties.BIRTHDAY, String.valueOf(currentTime));
            return currentTime;
        });
    }

    private void cleanCache() {
        cacheStore.delete(OPTIONS_KEY);
    }

    private void publishOptionUpdatedEvent() {
        flush();
        cleanCache();
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
    }
}

