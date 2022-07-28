package run.halo.app.theme.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.AbstractCacheManager;
import org.thymeleaf.cache.ExpressionCacheKey;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheEntryValidityChecker;
import org.thymeleaf.cache.StandardCache;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.cache.StandardParsedTemplateEntryValidator;
import org.thymeleaf.cache.TemplateCacheKey;
import org.thymeleaf.engine.TemplateModel;

/**
 * Code from {@link StandardCacheManager}.
 *
 * @author guqing
 * @see StandardCacheManager
 * @since 2.0.0
 */
public class ThemeCacheManager extends AbstractCacheManager {

    /**
     * Default template cache name: {@value}.
     */
    public static final String DEFAULT_TEMPLATE_CACHE_NAME = "TEMPLATE_CACHE";

    /**
     * Default template cache initial size: {@value}.
     */
    public static final int DEFAULT_TEMPLATE_CACHE_INITIAL_SIZE = 20;

    /**
     * Default template cache maximum size: {@value}.
     */
    public static final int DEFAULT_TEMPLATE_CACHE_MAX_SIZE = 200;

    /**
     * Default template cache "enable counters" flag: {@value}.
     */
    public static final boolean DEFAULT_TEMPLATE_CACHE_ENABLE_COUNTERS = false;

    /**
     * Default template cache "use soft references" flag: {@value}.
     */
    public static final boolean DEFAULT_TEMPLATE_CACHE_USE_SOFT_REFERENCES = true;

    /**
     * Default template cache logger name: null (default behaviour = org.thymeleaf.TemplateEngine
     * .cache.TEMPLATE_CACHE).
     */
    public static final String DEFAULT_TEMPLATE_CACHE_LOGGER_NAME = null;

    /**
     * Default template cache validity checker: an instance of
     * {@link StandardParsedTemplateEntryValidator}.
     */
    public static final ICacheEntryValidityChecker<TemplateCacheKey, TemplateModel>
        DEFAULT_TEMPLATE_CACHE_VALIDITY_CHECKER = new StandardParsedTemplateEntryValidator();


    /**
     * Default expression cache name: {@value}.
     */
    public static final String DEFAULT_EXPRESSION_CACHE_NAME = "EXPRESSION_CACHE";

    /**
     * Default expression cache initial size: {@value}.
     */
    public static final int DEFAULT_EXPRESSION_CACHE_INITIAL_SIZE = 100;

    /**
     * Default expression cache maximum size: {@value}.
     */
    public static final int DEFAULT_EXPRESSION_CACHE_MAX_SIZE = 500;

    /**
     * Default expression cache "enable counters" flag: {@value}.
     */
    public static final boolean DEFAULT_EXPRESSION_CACHE_ENABLE_COUNTERS = false;

    /**
     * Default expression cache "use soft references" flag: {@value}.
     */
    public static final boolean DEFAULT_EXPRESSION_CACHE_USE_SOFT_REFERENCES = true;

    /**
     * Default expression cache logger name: null (default behaviour = org.thymeleaf
     * .TemplateEngine.cache.EXPRESSION_CACHE).
     */
    public static final String DEFAULT_EXPRESSION_CACHE_LOGGER_NAME = null;

    /**
     * Default expression cache validity checker: null.
     */
    public static final ICacheEntryValidityChecker<ExpressionCacheKey, Object>
        DEFAULT_EXPRESSION_CACHE_VALIDITY_CHECKER = null;


    private String templateCacheName = DEFAULT_TEMPLATE_CACHE_NAME;
    private int templateCacheInitialSize = DEFAULT_TEMPLATE_CACHE_INITIAL_SIZE;
    private int templateCacheMaxSize = DEFAULT_TEMPLATE_CACHE_MAX_SIZE;
    private boolean templateCacheEnableCounters = DEFAULT_TEMPLATE_CACHE_ENABLE_COUNTERS;
    private boolean templateCacheUseSoftReferences = DEFAULT_TEMPLATE_CACHE_USE_SOFT_REFERENCES;
    private String templateCacheLoggerName = DEFAULT_TEMPLATE_CACHE_LOGGER_NAME;
    private ICacheEntryValidityChecker<TemplateCacheKey, TemplateModel>
        templateCacheValidityChecker = DEFAULT_TEMPLATE_CACHE_VALIDITY_CHECKER;

    private String expressionCacheName = DEFAULT_EXPRESSION_CACHE_NAME;
    private int expressionCacheInitialSize = DEFAULT_EXPRESSION_CACHE_INITIAL_SIZE;
    private int expressionCacheMaxSize = DEFAULT_EXPRESSION_CACHE_MAX_SIZE;
    private boolean expressionCacheEnableCounters = DEFAULT_EXPRESSION_CACHE_ENABLE_COUNTERS;
    private boolean expressionCacheUseSoftReferences = DEFAULT_EXPRESSION_CACHE_USE_SOFT_REFERENCES;
    private String expressionCacheLoggerName = DEFAULT_EXPRESSION_CACHE_LOGGER_NAME;
    private ICacheEntryValidityChecker<ExpressionCacheKey, Object> expressionCacheValidityChecker =
        DEFAULT_EXPRESSION_CACHE_VALIDITY_CHECKER;


    public ThemeCacheManager() {
        super();
    }


    @Override
    protected final ICache<TemplateCacheKey, TemplateModel> initializeTemplateCache() {
        final int maxSize = getTemplateCacheMaxSize();
        if (maxSize == 0) {
            return null;
        }
        StandardCache<TemplateCacheKey, TemplateModel> standardCache =
            new StandardCache<>(
                getTemplateCacheName(), getTemplateCacheUseSoftReferences(),
                getTemplateCacheInitialSize(), maxSize,
                getTemplateCacheValidityChecker(), getTemplateCacheLogger(),
                getTemplateCacheEnableCounters());
        return new ThemeTemplateCacheDecorator(standardCache);
    }


    @Override
    protected final ICache<ExpressionCacheKey, Object> initializeExpressionCache() {
        final int maxSize = getExpressionCacheMaxSize();
        if (maxSize == 0) {
            return null;
        }
        return new StandardCache<ExpressionCacheKey, Object>(
            getExpressionCacheName(), getExpressionCacheUseSoftReferences(),
            getExpressionCacheInitialSize(), maxSize,
            getExpressionCacheValidityChecker(), getExpressionCacheLogger(),
            getExpressionCacheEnableCounters());
    }


    public String getTemplateCacheName() {
        return this.templateCacheName;
    }

    public boolean getTemplateCacheUseSoftReferences() {
        return this.templateCacheUseSoftReferences;
    }

    private boolean getTemplateCacheEnableCounters() {
        return this.templateCacheEnableCounters;
    }

    public int getTemplateCacheInitialSize() {
        return this.templateCacheInitialSize;
    }

    public int getTemplateCacheMaxSize() {
        return this.templateCacheMaxSize;
    }

    public String getTemplateCacheLoggerName() {
        return this.templateCacheLoggerName;
    }

    public ICacheEntryValidityChecker<TemplateCacheKey,
        TemplateModel> getTemplateCacheValidityChecker() {
        return this.templateCacheValidityChecker;
    }

    public final Logger getTemplateCacheLogger() {
        final String loggerName = getTemplateCacheLoggerName();
        if (loggerName != null) {
            return LoggerFactory.getLogger(loggerName);
        }
        return LoggerFactory.getLogger(
            TemplateEngine.class.getName() + ".cache." + getTemplateCacheName());
    }


    public String getExpressionCacheName() {
        return this.expressionCacheName;
    }

    public boolean getExpressionCacheUseSoftReferences() {
        return this.expressionCacheUseSoftReferences;
    }

    private boolean getExpressionCacheEnableCounters() {
        return this.expressionCacheEnableCounters;
    }

    public int getExpressionCacheInitialSize() {
        return this.expressionCacheInitialSize;
    }

    public int getExpressionCacheMaxSize() {
        return this.expressionCacheMaxSize;
    }

    public String getExpressionCacheLoggerName() {
        return this.expressionCacheLoggerName;
    }

    public ICacheEntryValidityChecker<ExpressionCacheKey,
        Object> getExpressionCacheValidityChecker() {
        return this.expressionCacheValidityChecker;
    }

    public final Logger getExpressionCacheLogger() {
        final String loggerName = getExpressionCacheLoggerName();
        if (loggerName != null) {
            return LoggerFactory.getLogger(loggerName);
        }
        return LoggerFactory.getLogger(
            TemplateEngine.class.getName() + ".cache." + getExpressionCacheName());
    }


    public void setTemplateCacheName(final String templateCacheName) {
        this.templateCacheName = templateCacheName;
    }

    public void setTemplateCacheInitialSize(final int templateCacheInitialSize) {
        this.templateCacheInitialSize = templateCacheInitialSize;
    }

    public void setTemplateCacheMaxSize(final int templateCacheMaxSize) {
        this.templateCacheMaxSize = templateCacheMaxSize;
    }

    public void setTemplateCacheUseSoftReferences(final boolean templateCacheUseSoftReferences) {
        this.templateCacheUseSoftReferences = templateCacheUseSoftReferences;
    }

    public void setTemplateCacheLoggerName(final String templateCacheLoggerName) {
        this.templateCacheLoggerName = templateCacheLoggerName;
    }

    public void setTemplateCacheValidityChecker(
        final ICacheEntryValidityChecker<TemplateCacheKey,
            TemplateModel> templateCacheValidityChecker) {
        this.templateCacheValidityChecker = templateCacheValidityChecker;
    }

    public void setTemplateCacheEnableCounters(boolean templateCacheEnableCounters) {
        this.templateCacheEnableCounters = templateCacheEnableCounters;
    }


    public void setExpressionCacheName(final String expressionCacheName) {
        this.expressionCacheName = expressionCacheName;
    }

    public void setExpressionCacheInitialSize(final int expressionCacheInitialSize) {
        this.expressionCacheInitialSize = expressionCacheInitialSize;
    }

    public void setExpressionCacheMaxSize(final int expressionCacheMaxSize) {
        this.expressionCacheMaxSize = expressionCacheMaxSize;
    }

    public void setExpressionCacheUseSoftReferences(
        final boolean expressionCacheUseSoftReferences) {
        this.expressionCacheUseSoftReferences = expressionCacheUseSoftReferences;
    }

    public void setExpressionCacheLoggerName(final String expressionCacheLoggerName) {
        this.expressionCacheLoggerName = expressionCacheLoggerName;
    }

    public void setExpressionCacheValidityChecker(
        final ICacheEntryValidityChecker<ExpressionCacheKey,
            Object> expressionCacheValidityChecker) {
        this.expressionCacheValidityChecker = expressionCacheValidityChecker;
    }

    public void setExpressionCacheEnableCounters(boolean expressionCacheEnableCounters) {
        this.expressionCacheEnableCounters = expressionCacheEnableCounters;
    }

}
