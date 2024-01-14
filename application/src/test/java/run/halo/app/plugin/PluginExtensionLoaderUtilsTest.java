package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.isSetting;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.lookupExtensions;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

class PluginExtensionLoaderUtilsTest {

    @Test
    void lookupExtensionsAndIsSettingTest() throws IOException {
        var resourceLoader = new DefaultResourceLoader();
        var rootResource = resourceLoader.getResource("classpath:plugin/plugin-0.0.1/");
        var classLoader = new URLClassLoader(new URL[] {rootResource.getURL()}, null);
        var resources = lookupExtensions(classLoader);
        assertTrue(resources.length >= 1);
        var settingResource = Arrays.stream(resources)
            .filter(r -> Objects.equals("setting.yaml", r.getFilename()))
            .findFirst()
            .orElseThrow();

        var loader = new YamlUnstructuredLoader(settingResource);
        var unstructuredList = loader.load();
        assertEquals(1, unstructuredList.size());
        assertTrue(isSetting("fake-setting").test(unstructuredList.get(0)));
        assertFalse(isSetting("non-fake-setting").test(unstructuredList.get(0)));
        assertFalse(isSetting("").test(unstructuredList.get(0)));
        assertFalse(isSetting(null).test(unstructuredList.get(0)));
    }
}