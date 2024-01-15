package run.halo.app.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.util.Predicates;
import run.halo.app.core.extension.Setting;
import run.halo.app.extension.Unstructured;

@Slf4j
public class PluginExtensionLoaderUtils {
    static final String EXTENSION_LOCATION_PATTERN = "classpath:extensions/*.{ext:yaml|yml}";

    public static Predicate<Unstructured> isSetting(String settingName) {
        if (StringUtils.isBlank(settingName)) {
            return Predicates.isFalse();
        }
        var settingGk = Setting.GVK.groupKind();
        return unstructured -> {
            var gk = unstructured.groupVersionKind().groupKind();
            var name = unstructured.getMetadata().getName();
            return Objects.equals(settingName, name) && Objects.equals(settingGk, gk);
        };
    }

    public static Resource[] lookupExtensions(ClassLoader classLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Trying to lookup extensions from {}", classLoader);
        }
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            var urls = urlClassLoader.getURLs();
            // The parent class loader must be null here because we don't want to
            // get any resources from parent class loader.
            classLoader = new URLClassLoader(urls, null);
        }
        var resolver = new PathMatchingResourcePatternResolver(classLoader);
        try {
            var resources = resolver.getResources(EXTENSION_LOCATION_PATTERN);
            if (log.isDebugEnabled()) {
                log.debug("Looked up {} resources(s) from {}", resources.length, classLoader);
            }
            return resources;
        } catch (FileNotFoundException ignored) {
            // Ignore the exception only if extensions folder was not found.
        } catch (IOException e) {
            throw new RuntimeException(String.format("""
                Failed to get extension resources while resolving plugin setting \
                in class loader %s.\
                """, classLoader), e);
        }
        return new Resource[] {};
    }

}
