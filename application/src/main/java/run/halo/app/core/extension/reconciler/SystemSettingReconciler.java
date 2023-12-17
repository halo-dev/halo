package run.halo.app.core.extension.reconciler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkRuleChangedEvent;

/**
 * Reconciler for system settings.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class SystemSettingReconciler implements Reconciler<Reconciler.Request> {
    public static final String OLD_THEME_ROUTE_RULES = "halo.run/old-theme-route-rules";
    public static final String FINALIZER_NAME = "system-setting-protection";

    private final ExtensionClient client;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final ApplicationContext applicationContext;

    private final RouteRuleReconciler routeRuleReconciler = new RouteRuleReconciler();

    public SystemSettingReconciler(ExtensionClient client,
        SystemConfigurableEnvironmentFetcher environmentFetcher,
        ApplicationContext applicationContext) {
        this.client = client;
        this.environmentFetcher = environmentFetcher;
        this.applicationContext = applicationContext;
    }

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        if (!isSystemSetting(name)) {
            return new Result(false, null);
        }
        client.fetch(ConfigMap.class, name)
            .ifPresent(configMap -> {
                addFinalizerIfNecessary(configMap);
                routeRuleReconciler.reconcile(name);
                customizeSystem(name);
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new ConfigMap())
            .build();
    }

    private void customizeSystem(String name) {
        if (!SystemSetting.SYSTEM_CONFIG_DEFAULT.equals(name)) {
            return;
        }
        // configMap named system not found then create it by system-default
        Optional<ConfigMap> systemOpt = client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG);
        if (systemOpt.isPresent()) {
            return;
        }
        ConfigMap system = client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT)
            .map(configMap -> {
                // create a new configMap named system by system-default
                ConfigMap systemConfigMap = new ConfigMap();
                systemConfigMap.setMetadata(new Metadata());
                systemConfigMap.getMetadata().setName(SystemSetting.SYSTEM_CONFIG);
                systemConfigMap.setData(configMap.getData());
                return systemConfigMap;
            })
            .orElseGet(() -> {
                // empty configMap named system
                ConfigMap configMap = new ConfigMap();
                configMap.setMetadata(new Metadata());
                configMap.getMetadata().setName(SystemSetting.SYSTEM_CONFIG);
                configMap.setData(new HashMap<>());
                return configMap;
            });
        client.create(system);
    }

    private void addFinalizerIfNecessary(ConfigMap oldConfigMap) {
        if (SystemSetting.SYSTEM_CONFIG.equals(oldConfigMap.getMetadata().getName())) {
            return;
        }
        Set<String> finalizers = oldConfigMap.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(ConfigMap.class, oldConfigMap.getMetadata().getName())
            .ifPresent(configMap -> {
                Set<String> newFinalizers = configMap.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    configMap.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(configMap);
            });
    }

    class RouteRuleReconciler {

        public void reconcile(String name) {
            reconcileArchivesRule(name);
            reconcileTagsRule(name);
            reconcileCategoriesRule(name);
            reconcilePostRule(name);
        }

        private void reconcileArchivesRule(String name) {
            getConfigMap(name).ifPresent(configMap -> {
                SystemSetting.ThemeRouteRules oldRules = getOldRouteRulesFromAnno(configMap);
                SystemSetting.ThemeRouteRules newRules = getRouteRules(configMap);

                final String oldArchivesPrefix = oldRules.getArchives();
                final String oldPostPattern = oldRules.getPost();

                // dispatch event
                final boolean archivesPrefixChanged =
                    !StringUtils.equals(oldRules.getArchives(), newRules.getArchives());

                final boolean postPatternChanged =
                    changePostPatternPrefixIfNecessary(oldArchivesPrefix, newRules);

                if (archivesPrefixChanged || postPatternChanged) {
                    oldRules.setPost(newRules.getPost());
                    oldRules.setArchives(newRules.getArchives());
                    updateNewRuleToConfigMap(configMap, oldRules, newRules);
                }

                // archives rule changed
                if (archivesPrefixChanged) {
                    log.debug("Archives prefix changed from [{}] to [{}].", oldArchivesPrefix,
                        newRules.getArchives());
                    applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                        DefaultTemplateEnum.ARCHIVES,
                        oldArchivesPrefix, newRules.getArchives()));
                }

                if (postPatternChanged) {
                    log.debug("Post pattern changed from [{}] to [{}].", oldPostPattern,
                        newRules.getPost());
                    // post rule changed
                    applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                        DefaultTemplateEnum.POST, oldPostPattern, newRules.getPost()));
                }
            });
        }

        private void updateNewRuleToConfigMap(ConfigMap configMap,
            SystemSetting.ThemeRouteRules oldRules,
            SystemSetting.ThemeRouteRules newRules) {
            Map<String, String> annotations = getAnnotationsSafe(configMap);
            annotations.put(OLD_THEME_ROUTE_RULES, JsonUtils.objectToJson(oldRules));
            configMap.getData().put(SystemSetting.ThemeRouteRules.GROUP,
                JsonUtils.objectToJson(newRules));
            client.update(configMap);
        }

        private void reconcileTagsRule(String name) {
            getConfigMap(name).ifPresent(configMap -> {
                SystemSetting.ThemeRouteRules oldRules = getOldRouteRulesFromAnno(configMap);
                SystemSetting.ThemeRouteRules newRules = getRouteRules(configMap);
                final String oldTagsPrefix = oldRules.getTags();
                if (!StringUtils.equals(oldTagsPrefix, newRules.getTags())) {
                    oldRules.setTags(newRules.getTags());
                    updateNewRuleToConfigMap(configMap, oldRules, newRules);

                    log.debug("Tags prefix changed from [{}] to [{}].", oldTagsPrefix,
                        newRules.getTags());
                    // then publish event
                    applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                        DefaultTemplateEnum.TAGS,
                        oldTagsPrefix, newRules.getTags()));
                }
            });
        }

        private void reconcileCategoriesRule(String name) {
            getConfigMap(name).ifPresent(configMap -> {
                SystemSetting.ThemeRouteRules oldRules = getOldRouteRulesFromAnno(configMap);
                SystemSetting.ThemeRouteRules newRules = getRouteRules(configMap);
                final String oldCategoriesPrefix = oldRules.getCategories();
                if (!StringUtils.equals(oldCategoriesPrefix, newRules.getCategories())) {
                    oldRules.setCategories(newRules.getCategories());
                    updateNewRuleToConfigMap(configMap, oldRules, newRules);

                    log.debug("Categories prefix changed from [{}] to [{}].", oldCategoriesPrefix,
                        newRules.getCategories());
                    // categories rule changed
                    applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                        DefaultTemplateEnum.CATEGORIES,
                        oldCategoriesPrefix, newRules.getCategories()));
                }
            });
        }

        private void reconcilePostRule(String name) {
            getConfigMap(name).ifPresent(configMap -> {
                SystemSetting.ThemeRouteRules oldRules = getOldRouteRulesFromAnno(configMap);
                SystemSetting.ThemeRouteRules newRules = getRouteRules(configMap);

                final String oldPostPattern = oldRules.getPost();
                if (!StringUtils.equals(oldPostPattern, newRules.getPost())) {
                    oldRules.setPost(newRules.getPost());
                    updateNewRuleToConfigMap(configMap, oldRules, newRules);

                    log.debug("Post pattern changed from [{}] to [{}].", oldPostPattern,
                        newRules.getPost());
                    // post rule changed
                    applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                        DefaultTemplateEnum.POST,
                        oldPostPattern, newRules.getPost()));
                }
            });
        }

        static boolean changePostPatternPrefixIfNecessary(String oldArchivePrefix,
            SystemSetting.ThemeRouteRules newRules) {
            if (StringUtils.isBlank(oldArchivePrefix)
                || StringUtils.isBlank(newRules.getPost())) {
                return false;
            }
            String newArchivesPrefix = newRules.getArchives();
            if (StringUtils.equals(oldArchivePrefix, newArchivesPrefix)) {
                return false;
            }

            String oldPrefix = StringUtils.removeStart(oldArchivePrefix, "/");
            String postPattern = StringUtils.removeStart(newRules.getPost(), "/");

            if (postPattern.startsWith(oldPrefix)) {
                String postPatternToUpdate = PathUtils.combinePath(newArchivesPrefix,
                    StringUtils.removeStart(postPattern, oldPrefix));
                newRules.setPost(postPatternToUpdate);
                return true;
            }
            return false;
        }

        private SystemSetting.ThemeRouteRules getOldRouteRulesFromAnno(ConfigMap configMap) {
            Map<String, String> annotations = getAnnotationsSafe(configMap);
            String oldRulesJson = annotations.get(OLD_THEME_ROUTE_RULES);

            // old rules is empty, means this is the first time to update theme route rules
            if (oldRulesJson == null) {
                oldRulesJson = "{}";
            }

            // diff old rules and new rules
            return JsonUtils.jsonToObject(oldRulesJson, SystemSetting.ThemeRouteRules.class);
        }

        private SystemSetting.ThemeRouteRules getRouteRules(ConfigMap configMap) {
            Map<String, String> data = configMap.getData();
            // get new rules and replace old rules to new rules
            return JsonUtils.jsonToObject(data.get(SystemSetting.ThemeRouteRules.GROUP),
                SystemSetting.ThemeRouteRules.class);
        }

        private Map<String, String> getAnnotationsSafe(ConfigMap configMap) {
            Map<String, String> annotations = configMap.getMetadata().getAnnotations();
            if (annotations == null) {
                annotations = new HashMap<>();
                configMap.getMetadata().setAnnotations(annotations);
            }
            return annotations;
        }
    }

    public boolean isSystemSetting(String name) {
        return SystemSetting.SYSTEM_CONFIG.equals(name)
            || SystemSetting.SYSTEM_CONFIG_DEFAULT.equals(name);
    }

    private Optional<ConfigMap> getConfigMap(String name) {
        return environmentFetcher.getConfigMapBlocking();
    }
}
