package run.halo.app.core.extension.reconciler;

import java.util.HashMap;
import java.util.Map;
import org.thymeleaf.util.StringUtils;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
public class SystemSettingReconciler implements Reconciler<Reconciler.Request> {

    private static final String OLD_THEME_ROUTE_RULES = "halo.run/old-theme-route-rules";

    private final ExtensionClient client;

    public SystemSettingReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        if (isSystemSetting(name)) {
            return new Result(false, null);
        }
        client.fetch(ConfigMap.class, name).ifPresent(configMap -> {


            client.update(configMap);
        });
        return null;
    }

    private void ruleChangedDispatcher(ConfigMap configMap, String name) {
        Map<String, String> data = configMap.getData();

        Map<String, String> annotations = getAnnotationsSafe(configMap);
        String oldRulesJson = annotations.get(OLD_THEME_ROUTE_RULES);

        String themeSettingJson = data.get(SystemSetting.Theme.GROUP);
        SystemSetting.Theme themeSetting =
            JsonUtils.jsonToObject(themeSettingJson, SystemSetting.Theme.class);

        // get new rules and replace old rules to new rules
        SystemSetting.ThemeRouteRules newRouteRules = themeSetting.getRouteRules();
        String newRulesJson = JsonUtils.objectToJson(newRouteRules);
        annotations.put(OLD_THEME_ROUTE_RULES, JsonUtils.objectToJson(newRulesJson));

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
        }

        if (!StringUtils.equals(oldRules.getTags(), newRouteRules.getTags())) {
            // tags rule changed
        }

        if (!StringUtils.equals(oldRules.getCategories(), newRouteRules.getCategories())) {
            // categories rule changed
        }

        if (!StringUtils.equals(oldRules.getPost(), newRouteRules.getPost())) {
            // categories rule changed
        }
    }

    private void regeneratePostPermalink(String pattern) {

    }

    private void regenerateArchivesRoute(String prefix) {

    }

    private void regenerateTagsRoute(String prefix) {

    }

    private void regenerateCategoriesRoute(String prefix) {

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
        return SystemConfigurableEnvironmentFetcher.SYSTEM_CONFIGMAP_NAME.equals(name);
    }
}
