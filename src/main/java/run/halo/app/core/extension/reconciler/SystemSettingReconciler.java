package run.halo.app.core.extension.reconciler;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
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
public class SystemSettingReconciler implements Reconciler<Reconciler.Request> {

    private static final String OLD_THEME_ROUTE_RULES = "halo.run/old-theme-route-rules";

    private final ExtensionClient client;
    private final ApplicationContext applicationContext;

    public SystemSettingReconciler(ExtensionClient client, ApplicationContext applicationContext) {
        this.client = client;
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
                ConfigMap oldConfigMap = JsonUtils.deepCopy(configMap);

                ruleChangedDispatcher(configMap);

                if (!configMap.equals(oldConfigMap)) {
                    client.update(configMap);
                }
            });
        return new Result(false, null);
    }

    private void ruleChangedDispatcher(ConfigMap configMap) {
        Map<String, String> data = configMap.getData();

        Map<String, String> annotations = getAnnotationsSafe(configMap);
        String oldRulesJson = annotations.get(OLD_THEME_ROUTE_RULES);

        String routeRulesJson = data.get(SystemSetting.ThemeRouteRules.GROUP);
        // get new rules and replace old rules to new rules
        SystemSetting.ThemeRouteRules newRouteRules =
            JsonUtils.jsonToObject(routeRulesJson, SystemSetting.ThemeRouteRules.class);

        // old rules is empty, means this is the first time to update theme route rules
        if (oldRulesJson == null) {
            oldRulesJson = "{}";
        }

        // diff old rules and new rules
        SystemSetting.ThemeRouteRules oldRules =
            JsonUtils.jsonToObject(oldRulesJson, SystemSetting.ThemeRouteRules.class);

        // dispatch event
        if (!StringUtils.equals(oldRules.getArchives(), newRouteRules.getArchives())) {
            // archives rule changed
            applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                DefaultTemplateEnum.ARCHIVES,
                newRouteRules.getArchives()));
        }

        if (!StringUtils.equals(oldRules.getTags(), newRouteRules.getTags())) {
            // tags rule changed
            applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                DefaultTemplateEnum.TAGS,
                newRouteRules.getTags()));
        }

        if (!StringUtils.equals(oldRules.getCategories(), newRouteRules.getCategories())) {
            // categories rule changed
            applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                DefaultTemplateEnum.CATEGORIES,
                newRouteRules.getCategories()));
        }

        if (!StringUtils.equals(oldRules.getPost(), newRouteRules.getPost())) {
            // post rule changed
            applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                DefaultTemplateEnum.POST,
                newRouteRules.getPost()));
        }

        // TODO 此处立即同步 post 的新 pattern 到数据库，才能更新到文章页面的 permalink 地址
        //   但会导致乐观锁失效会失败一次 reconcile
        if (changePostPatternPrefixIfNecessary(oldRules, newRouteRules)) {
            data.put(SystemSetting.ThemeRouteRules.GROUP, JsonUtils.objectToJson(newRouteRules));
            annotations.put(OLD_THEME_ROUTE_RULES, JsonUtils.objectToJson(newRouteRules));
            // update config map immediately
            client.update(configMap);
        }

        // update theme setting
        data.put(SystemSetting.ThemeRouteRules.GROUP, JsonUtils.objectToJson(newRouteRules));
        annotations.put(OLD_THEME_ROUTE_RULES, JsonUtils.objectToJson(newRouteRules));
    }

    boolean changePostPatternPrefixIfNecessary(SystemSetting.ThemeRouteRules oldRules,
        SystemSetting.ThemeRouteRules newRules) {
        if (StringUtils.isBlank(oldRules.getArchives())
            || StringUtils.isBlank(newRules.getPost())) {
            return false;
        }
        String oldArchivesPrefix = StringUtils.removeStart(oldRules.getArchives(), "/");

        String postPattern = StringUtils.removeStart(newRules.getPost(), "/");
        String newArchivesPrefix = newRules.getArchives();
        if (postPattern.startsWith(oldArchivesPrefix)) {
            String postPatternToUpdate = PathUtils.combinePath(newArchivesPrefix,
                StringUtils.removeStart(postPattern, oldArchivesPrefix));
            newRules.setPost(postPatternToUpdate);

            // post rule changed
            applicationContext.publishEvent(new PermalinkRuleChangedEvent(this,
                DefaultTemplateEnum.POST, postPatternToUpdate));
            return true;
        }
        return false;
    }

    private Map<String, String> getAnnotationsSafe(ConfigMap configMap) {
        Map<String, String> annotations = configMap.getMetadata().getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>();
            configMap.getMetadata().setAnnotations(annotations);
        }
        return annotations;
    }

    public boolean isSystemSetting(String name) {
        return SystemSetting.SYSTEM_CONFIG.equals(name);
    }
}
