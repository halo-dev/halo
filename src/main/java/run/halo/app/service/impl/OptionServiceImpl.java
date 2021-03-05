package run.halo.app.service.impl;

import com.qiniu.common.Zone;
import com.qiniu.storage.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.exception.MissingPropertyException;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.dto.OptionSimpleDTO;
import run.halo.app.model.entity.Option;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.model.enums.ValueEnum;
import run.halo.app.model.params.OptionParam;
import run.halo.app.model.params.OptionQuery;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.properties.OtherProperties;
import run.halo.app.model.properties.PermalinkProperties;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.model.properties.QiniuOssProperties;
import run.halo.app.model.properties.SeoProperties;
import run.halo.app.repository.OptionRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.ValidationUtils;

/**
 * OptionService implementation class
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-03-14
 */
@Slf4j
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer>
    implements OptionService {

    private final OptionRepository optionRepository;
    private final ApplicationContext applicationContext;
    private final AbstractStringCacheStore cacheStore;
    private final Map<String, PropertyEnum> propertyEnumMap;
    private final ApplicationEventPublisher eventPublisher;

    public OptionServiceImpl(OptionRepository optionRepository,
        ApplicationContext applicationContext,
        AbstractStringCacheStore cacheStore,
        ApplicationEventPublisher eventPublisher) {
        super(optionRepository);
        this.optionRepository = optionRepository;
        this.applicationContext = applicationContext;
        this.cacheStore = cacheStore;
        this.eventPublisher = eventPublisher;

        propertyEnumMap = Collections.unmodifiableMap(PropertyEnum.getValuePropertyEnumMap());
    }

    @Deprecated
    @Transactional
    private void save(@NonNull String key, @Nullable String value) {
        Assert.hasText(key, "Option key must not be blank");
        save(Collections.singletonMap(key, value));
    }

    @Override
    @Transactional
    public void save(Map<String, Object> optionMap) {
        if (CollectionUtils.isEmpty(optionMap)) {
            return;
        }

        Map<String, Option> optionKeyMap = ServiceUtils.convertToMap(listAll(), Option::getKey);

        List<Option> optionsToCreate = new LinkedList<>();
        List<Option> optionsToUpdate = new LinkedList<>();

        optionMap.forEach((key, value) -> {
            Option oldOption = optionKeyMap.get(key);
            if (oldOption == null || !StringUtils.equals(oldOption.getValue(), value.toString())) {
                OptionParam optionParam = new OptionParam();
                optionParam.setKey(key);
                optionParam.setValue(value.toString());
                ValidationUtils.validate(optionParam);

                if (oldOption == null) {
                    // Create it
                    optionsToCreate.add(optionParam.convertTo());
                } else if (!StringUtils.equals(oldOption.getValue(), value.toString())) {
                    // Update it
                    optionParam.update(oldOption);
                    optionsToUpdate.add(oldOption);
                }
            }
        });

        // Update them
        updateInBatch(optionsToUpdate);

        // Create them
        createInBatch(optionsToCreate);

        if (!CollectionUtils.isEmpty(optionsToUpdate)
            || !CollectionUtils.isEmpty(optionsToCreate)) {
            // If there is something changed
            eventPublisher.publishEvent(new OptionUpdatedEvent(this));
        }

    }

    @Override
    public void save(List<OptionParam> optionParams) {
        if (CollectionUtils.isEmpty(optionParams)) {
            return;
        }

        Map<String, Object> optionMap =
            ServiceUtils.convertToMap(optionParams, OptionParam::getKey, OptionParam::getValue);
        save(optionMap);
    }

    @Override
    public void save(OptionParam optionParam) {
        Option option = optionParam.convertTo();
        create(option);
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
    }

    @Override
    public void update(Integer optionId, OptionParam optionParam) {
        Option optionToUpdate = getById(optionId);
        optionParam.update(optionToUpdate);
        update(optionToUpdate);
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
    }

    @Override
    public void saveProperty(PropertyEnum property, String value) {
        Assert.notNull(property, "Property must not be null");

        save(property.getValue(), value);
    }

    @Override
    public void saveProperties(Map<? extends PropertyEnum, String> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }

        Map<String, Object> optionMap = new LinkedHashMap<>();

        properties.forEach((property, value) -> optionMap.put(property.getValue(), value));

        save(optionMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> listOptions() {
        // Get options from cache
        return cacheStore.getAny(OPTIONS_KEY, Map.class).orElseGet(() -> {
            List<Option> options = listAll();

            Set<String> keys = ServiceUtils.fetchProperty(options, Option::getKey);

            Map<String, Object> userDefinedOptionMap =
                ServiceUtils.convertToMap(options, Option::getKey, option -> {
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

                    result.put(key,
                        PropertyEnum.convertTo(propertyEnum.defaultValue(), propertyEnum));
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
    public Page<OptionSimpleDTO> pageDtosBy(Pageable pageable, OptionQuery optionQuery) {
        Assert.notNull(pageable, "Page info must not be null");

        Page<Option> optionPage = optionRepository.findAll(buildSpecByQuery(optionQuery), pageable);

        return optionPage.map(this::convertToDto);
    }

    @Override
    public Option removePermanently(Integer id) {
        Option deletedOption = removeById(id);
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
        return deletedOption;
    }

    @NonNull
    private Specification<Option> buildSpecByQuery(@NonNull OptionQuery optionQuery) {
        Assert.notNull(optionQuery, "Option query must not be null");

        return (Specification<Option>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (optionQuery.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), optionQuery.getType()));
            }

            if (optionQuery.getKeyword() != null) {

                String likeCondition =
                    String.format("%%%s%%", StringUtils.strip(optionQuery.getKeyword()));

                Predicate keyLike = criteriaBuilder.like(root.get("key"), likeCondition);

                Predicate valueLike = criteriaBuilder.like(root.get("value"), likeCondition);

                predicates.add(criteriaBuilder.or(keyLike, valueLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    @Override
    public Object getByKeyOfNullable(String key) {
        return getByKey(key).orElse(null);
    }

    @Override
    public Object getByKeyOfNonNull(String key) {
        return getByKey(key).orElseThrow(
            () -> new MissingPropertyException("You have to config " + key + " setting"));
    }

    @Override
    public Optional<Object> getByKey(String key) {
        Assert.hasText(key, "Option key must not be blank");

        return Optional.ofNullable(listOptions().get(key));
    }

    @Override
    public <T> Optional<T> getByKey(String key, Class<T> valueType) {
        return getByKey(key).map(value -> PropertyEnum.convertTo(value.toString(), valueType));
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
    public <T> Optional<T> getByProperty(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property)
            .map(propertyValue -> PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }

    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType,
        T defaultValue) {
        Assert.notNull(property, "Blog property must not be null");

        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property, propertyType).orElse(property.defaultValue(propertyType));
    }

    @Override
    public <T> T getByKeyOrDefault(String key, Class<T> valueType, T defaultValue) {
        return getByKey(key, valueType).orElse(defaultValue);
    }

    @Override
    public <T extends Enum<T>> Optional<T> getEnumByProperty(PropertyEnum property,
        Class<T> valueType) {
        return getByProperty(property)
            .map(value -> PropertyEnum.convertToEnum(value.toString(), valueType));
    }

    @Override
    public <T extends Enum<T>> T getEnumByPropertyOrDefault(PropertyEnum property,
        Class<T> valueType, T defaultValue) {
        return getEnumByProperty(property, valueType).orElse(defaultValue);
    }

    @Override
    public <V, E extends ValueEnum<V>> Optional<E> getValueEnumByProperty(PropertyEnum property,
        Class<V> valueType, Class<E> enumType) {
        return getByProperty(property).map(value -> ValueEnum
            .valueToEnum(enumType, PropertyEnum.convertTo(value.toString(), valueType)));
    }

    @Override
    public <V, E extends ValueEnum<V>> E getValueEnumByPropertyOrDefault(PropertyEnum property,
        Class<V> valueType, Class<E> enumType, E defaultValue) {
        return getValueEnumByProperty(property, valueType, enumType).orElse(defaultValue);
    }

    @Override
    public int getPostPageSize() {
        try {
            return getByPropertyOrDefault(PostProperties.INDEX_PAGE_SIZE, Integer.class,
                DEFAULT_POST_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(PostProperties.INDEX_PAGE_SIZE.getValue() + " option is not a number format",
                e);
            return DEFAULT_POST_PAGE_SIZE;
        }
    }

    @Override
    public int getArchivesPageSize() {
        try {
            return getByPropertyOrDefault(PostProperties.ARCHIVES_PAGE_SIZE, Integer.class,
                DEFAULT_ARCHIVES_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(
                PostProperties.ARCHIVES_PAGE_SIZE.getValue() + " option is not a number format", e);
            return DEFAULT_POST_PAGE_SIZE;
        }
    }

    @Override
    public int getCommentPageSize() {
        try {
            return getByPropertyOrDefault(CommentProperties.PAGE_SIZE, Integer.class,
                DEFAULT_COMMENT_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(CommentProperties.PAGE_SIZE.getValue() + " option is not a number format", e);
            return DEFAULT_COMMENT_PAGE_SIZE;
        }
    }

    @Override
    public int getRssPageSize() {
        try {
            return getByPropertyOrDefault(PostProperties.RSS_PAGE_SIZE, Integer.class,
                DEFAULT_RSS_PAGE_SIZE);
        } catch (NumberFormatException e) {
            log.error(PostProperties.RSS_PAGE_SIZE.getValue() + " setting is not a number format",
                e);
            return DEFAULT_RSS_PAGE_SIZE;
        }
    }

    @Override
    public Zone getQnYunZone() {
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

    @Override
    public Region getQiniuRegion() {
        return getByProperty(QiniuOssProperties.OSS_ZONE).map(qiniuZone -> {

            Region region;
            switch (qiniuZone.toString()) {
                case "z0":
                    region = Region.region0();
                    break;
                case "z1":
                    region = Region.region1();
                    break;
                case "z2":
                    region = Region.region2();
                    break;
                case "na0":
                    region = Region.regionNa0();
                    break;
                case "as0":
                    region = Region.regionAs0();
                    break;
                default:
                    // Default is detecting zone automatically
                    region = Region.autoRegion();
            }
            return region;

        }).orElseGet(Region::autoRegion);
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

        if (StringUtils.isNotBlank(blogUrl)) {
            blogUrl = StringUtils.removeEnd(blogUrl, "/");
        } else {
            blogUrl = String.format("http://%s:%s", "127.0.0.1", serverPort);
        }

        return blogUrl;
    }

    @Override
    public String getBlogTitle() {
        return getByProperty(BlogProperties.BLOG_TITLE).orElse("").toString();
    }

    @Override
    public String getSeoKeywords() {
        return getByProperty(SeoProperties.KEYWORDS).orElse("").toString();
    }

    @Override
    public String getSeoDescription() {
        return getByProperty(SeoProperties.DESCRIPTION).orElse("").toString();
    }

    @Override
    public long getBirthday() {
        return getByProperty(PrimaryProperties.BIRTHDAY, Long.class).orElseGet(() -> {
            long currentTime = DateUtils.now().getTime();
            saveProperty(PrimaryProperties.BIRTHDAY, String.valueOf(currentTime));
            return currentTime;
        });
    }

    @Override
    public PostPermalinkType getPostPermalinkType() {
        return getEnumByPropertyOrDefault(PermalinkProperties.POST_PERMALINK_TYPE,
            PostPermalinkType.class, PostPermalinkType.DEFAULT);
    }

    @Override
    public SheetPermalinkType getSheetPermalinkType() {
        return getEnumByPropertyOrDefault(PermalinkProperties.SHEET_PERMALINK_TYPE,
            SheetPermalinkType.class, SheetPermalinkType.SECONDARY);
    }

    @Override
    public String getSheetPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.SHEET_PREFIX, String.class,
            PermalinkProperties.SHEET_PREFIX.defaultValue());
    }

    @Override
    public String getLinksPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.LINKS_PREFIX, String.class,
            PermalinkProperties.LINKS_PREFIX.defaultValue());
    }

    @Override
    public String getPhotosPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.PHOTOS_PREFIX, String.class,
            PermalinkProperties.PHOTOS_PREFIX.defaultValue());
    }

    @Override
    public String getJournalsPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.JOURNALS_PREFIX, String.class,
            PermalinkProperties.JOURNALS_PREFIX.defaultValue());
    }

    @Override
    public String getArchivesPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class,
            PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
    }

    @Override
    public String getCategoriesPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.CATEGORIES_PREFIX, String.class,
            PermalinkProperties.CATEGORIES_PREFIX.defaultValue());
    }

    @Override
    public String getTagsPrefix() {
        return getByPropertyOrDefault(PermalinkProperties.TAGS_PREFIX, String.class,
            PermalinkProperties.TAGS_PREFIX.defaultValue());
    }

    @Override
    public String getPathSuffix() {
        return getByPropertyOrDefault(PermalinkProperties.PATH_SUFFIX, String.class,
            PermalinkProperties.PATH_SUFFIX.defaultValue());
    }

    @Override
    public Boolean isEnabledAbsolutePath() {
        return getByPropertyOrDefault(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.class,
            true);
    }

    @Override
    public List<OptionDTO> replaceUrl(String oldUrl, String newUrl) {
        List<Option> options = listAll();
        List<Option> replaced = new ArrayList<>();
        options.forEach(option -> {
            if (StringUtils.isNotEmpty(option.getValue())) {
                option.setValue(option.getValue().replaceAll(oldUrl, newUrl));
            }
            replaced.add(option);
        });
        List<Option> updated = updateInBatch(replaced);
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
        return updated.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public OptionSimpleDTO convertToDto(Option option) {
        Assert.notNull(option, "Option must not be null");

        return new OptionSimpleDTO().convertFrom(option);
    }

    @Deprecated
    private void cleanCache() {
        cacheStore.delete(OPTIONS_KEY);
    }

    @Deprecated
    private void publishOptionUpdatedEvent() {
        flush();
        cleanCache();
        eventPublisher.publishEvent(new OptionUpdatedEvent(this));
    }
}

