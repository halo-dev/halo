package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.SystemSetting;

/**
 * Tests for {@link SystemSettingReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SystemSettingReconcilerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private ApplicationContext applicationContext;

    private SystemSettingReconciler systemSettingReconciler;

    @BeforeEach
    void setUp() {
        systemSettingReconciler = new SystemSettingReconciler(client, applicationContext);
    }

    @Test
    void changePostPatternPrefixIfNecessary() {
        SystemSetting.ThemeRouteRules oldRouteRules = new SystemSetting.ThemeRouteRules();
        oldRouteRules.setPost("/archives/{slug}");
        oldRouteRules.setArchives("archives");

        SystemSetting.ThemeRouteRules newRouteRules = new SystemSetting.ThemeRouteRules();
        newRouteRules.setPost("/archives/{slug}");
        newRouteRules.setArchives("new");
        boolean result = systemSettingReconciler.changePostPatternPrefixIfNecessary(oldRouteRules,
            newRouteRules);
        assertThat(result).isTrue();

        assertThat(newRouteRules.getPost()).isEqualTo("/new/{slug}");
    }
}