package run.halo.app.infra;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link InitializationStateGetter}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class InitializationStateGetterTest {
    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private DefaultInitializationStateGetter initializationStateGetter;

    @Test
    void userInitialized() {
        when(client.list(eq(User.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.empty());
        initializationStateGetter.userInitialized()
            .as(StepVerifier::create)
            .expectNext(false)
            .verifyComplete();

        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-hidden-user");
        user.getMetadata().setLabels(Map.of("halo.run/hidden-user", "true"));
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName("fake-hidden-user");
        ListResult<User> listResult = new ListResult<>(List.of(user));

        when(client.list(eq(User.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(listResult));
        initializationStateGetter.userInitialized()
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void dataInitialized() {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(SystemState.SYSTEM_STATES_CONFIGMAP);
        configMap.setData(Map.of("states", "{\"isSetup\":true}"));
        when(client.fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP)))
            .thenReturn(Mono.just(configMap));
        initializationStateGetter.dataInitialized()
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();

        // call again
        initializationStateGetter.dataInitialized()
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();
        // execute only once
        verify(client).fetch(eq(ConfigMap.class), eq(SystemState.SYSTEM_STATES_CONFIGMAP));
    }
}