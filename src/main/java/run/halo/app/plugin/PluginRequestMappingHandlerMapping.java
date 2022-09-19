package run.halo.app.plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.pf4j.PluginRuntimeException;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import run.halo.app.extension.GroupVersion;

/**
 * An extension of {@link RequestMappingInfoHandlerMapping} that creates
 * {@link RequestMappingInfo} instances from class-level and method-level
 * {@link RequestMapping} annotations used by plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PluginRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final MultiValueMap<String, RequestMappingInfo> pluginMappingInfo =
        new LinkedMultiValueMap<>();

    @Override
    protected void initHandlerMethods() {
        // Parent method will scan beans in the ApplicationContext
        // detect and register handler methods.
        // but this is superfluous for this class.
    }

    /**
     * Register handler methods according to the plugin id and the controller(annotated
     * {@link Controller}) bean.
     *
     * @param pluginId plugin id to be registered
     * @param handler controller bean
     */
    public void registerHandlerMethods(String pluginId, Object handler) {
        Class<?> handlerType = (handler instanceof String beanName
            ? obtainApplicationContext().getType(beanName) : handler.getClass());

        if (handlerType != null) {
            final Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>)
                    method -> getPluginMappingForMethod(pluginId, method, userType));
            if (logger.isTraceEnabled()) {
                logger.trace(formatMappings(userType, methods));
            } else if (mappingsLogger.isDebugEnabled()) {
                mappingsLogger.debug(formatMappings(userType, methods));
            }
            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handler, invocableMethod, mapping);
                pluginMappingInfo.add(pluginId, mapping);
            });
        }
    }

    private String formatMappings(Class<?> userType, Map<Method, RequestMappingInfo> methods) {
        String packageName = ClassUtils.getPackageName(userType);
        String formattedType = (StringUtils.hasText(packageName)
            ? Arrays.stream(packageName.split("\\."))
            .map(packageSegment -> packageSegment.substring(0, 1))
            .collect(Collectors.joining(".", "", "." + userType.getSimpleName())) :
            userType.getSimpleName());
        Function<Method, String> methodFormatter =
            method -> Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(",", "(", ")"));
        return methods.entrySet().stream()
            .map(e -> {
                Method method = e.getKey();
                return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
            })
            .collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
    }

    /**
     * Remove handler methods and mapping based on plugin id.
     *
     * @param pluginId plugin id
     */
    public void unregister(String pluginId) {
        Assert.notNull(pluginId, "The pluginId must not be null.");
        if (!pluginMappingInfo.containsKey(pluginId)) {
            return;
        }
        pluginMappingInfo.remove(pluginId).forEach(this::unregisterMapping);
    }

    protected List<RequestMappingInfo> getMappings(String pluginId) {
        List<RequestMappingInfo> requestMappingInfos = pluginMappingInfo.get(pluginId);
        if (requestMappingInfos == null) {
            return Collections.emptyList();
        }
        return List.copyOf(requestMappingInfos);
    }

    protected RequestMappingInfo getPluginMappingForMethod(String pluginId,
        Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info != null) {
            ApiVersion apiVersion = handlerType.getAnnotation(ApiVersion.class);
            if (apiVersion == null) {
                throw new PluginRuntimeException(
                    "The handler [" + handlerType + "] is missing @ApiVersion annotation.");
            }
            info = RequestMappingInfo.paths(buildPrefix(pluginId, apiVersion.value())).build()
                .combine(info);
        }
        return info;
    }

    protected String buildPrefix(String pluginId, String version) {
        GroupVersion groupVersion = GroupVersion.parseAPIVersion(version);
        return String.format("/apis/api.plugin.halo.run/%s/plugins/%s", groupVersion.version(),
            pluginId);
    }
}
