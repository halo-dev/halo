package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.theme.finders.vo.SiteSettingVo;

/**
 * Tests for {@link SiteSettingVariablesAcquirer}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class SiteSettingVariablesAcquirerTest {
    @Mock
    private ExternalUrlSupplier externalUrlSupplier;
    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @InjectMocks
    private SiteSettingVariablesAcquirer siteSettingVariablesAcquirer;

    @Test
    void acquireWhenExternalUrlSet() throws MalformedURLException {
        var configMap = new ConfigMap();
        configMap.setData(Map.of());

        var url = new URL("https://halo.run");
        when(externalUrlSupplier.getURL(any())).thenReturn(url);
        when(environmentFetcher.getConfigMap()).thenReturn(Mono.just(configMap));

        siteSettingVariablesAcquirer.acquire(mock(ServerWebExchange.class))
            .as(StepVerifier::create)
            .consumeNextWith(result -> {
                assertThat(result).containsKey("site");
                assertThat(result.get("site")).isInstanceOf(SiteSettingVo.class);
                assertThat((SiteSettingVo) result.get("site"))
                    .extracting(SiteSettingVo::getUrl)
                    .isEqualTo(url);
            })
            .verifyComplete();
        verify(externalUrlSupplier).getURL(any());
    }
}
