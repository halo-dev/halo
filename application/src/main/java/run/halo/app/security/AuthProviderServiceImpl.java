package run.halo.app.security;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A default implementation of {@link AuthProviderService}.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class AuthProviderServiceImpl implements AuthProviderService {
    private final ReactiveExtensionClient client;
    private final ObjectProvider<SystemConfigurableEnvironmentFetcher> environmentFetcherProvider;

    @Override
    public Mono<AuthProvider> enable(String name) {
        return client.get(AuthProvider.class, name)
            .flatMap(authProvider -> updateAuthProviderEnabled(name, true)
                .thenReturn(authProvider)
            );
    }

    @Override
    public Mono<AuthProvider> disable(String name) {
        return client.get(AuthProvider.class, name)
            // privileged auth provider cannot be disabled
            .filter(authProvider -> !privileged(authProvider))
            .flatMap(authProvider -> updateAuthProviderEnabled(name, false)
                .thenReturn(authProvider)
            );
    }

    @Override
    public Mono<List<ListedAuthProvider>> listAll() {
        var listOptions = ListOptions.builder()
            .andQuery(ExtensionUtil.notDeleting())
            .build();
        var allProvidersMono =
            client.listAll(AuthProvider.class, listOptions, ExtensionUtil.defaultSort())
                .map(this::convertTo)
                .collectList()
                .subscribeOn(Schedulers.boundedElastic());

        var boundProvidersMono = listMyConnections()
            .map(connection -> connection.getSpec().getRegistrationId())
            .collect(Collectors.toSet())
            .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(allProvidersMono, boundProvidersMono, fetchProviderStates())
            .map(tuple3 -> {
                var allProviders = tuple3.getT1();
                var boundProviderNames = tuple3.getT2();
                var stateMap = tuple3.getT3().stream()
                    .collect(Collectors.toMap(SystemSetting.AuthProviderState::getName,
                        Function.identity()));
                return allProviders.stream()
                    .peek(authProvider -> {
                        authProvider.setIsBound(
                            boundProviderNames.contains(authProvider.getName()));
                        authProvider.setEnabled(false);
                        // set enabled state and priority
                        var state = stateMap.get(authProvider.getName());
                        if (state != null) {
                            authProvider.setEnabled(state.isEnabled());
                            authProvider.setPriority(state.getPriority());
                        }
                    })
                    .sorted(Comparator.comparingInt(ListedAuthProvider::getPriority)
                        .thenComparing(ListedAuthProvider::getName))
                    .toList();
            });
    }

    @Override
    public Flux<AuthProvider> getEnabledProviders() {
        return fetchProviderStates().flatMapMany(states -> {
            var namePriorityMap = states.stream()
                // filter enabled providers
                .filter(SystemSetting.AuthProviderState::isEnabled)
                .collect(Collectors.toMap(SystemSetting.AuthProviderState::getName,
                    SystemSetting.AuthProviderState::getPriority));

            var listOptions = ListOptions.builder()
                .andQuery(QueryFactory.in("metadata.name", namePriorityMap.keySet()))
                .andQuery(ExtensionUtil.notDeleting())
                .build();
            return client.listAll(AuthProvider.class, listOptions, ExtensionUtil.defaultSort())
                .map(provider -> new AuthProviderWithPriority()
                    .setAuthProvider(provider)
                    .setPriority(namePriorityMap.getOrDefault(
                        provider.getMetadata().getName(), 0)
                    )
                )
                .sort(AuthProviderWithPriority::compareTo)
                .map(AuthProviderWithPriority::getAuthProvider);
        });
    }

    @Data
    @Accessors(chain = true)
    static class AuthProviderWithPriority implements Comparable<AuthProviderWithPriority> {
        private AuthProvider authProvider;
        private int priority;

        public String getName() {
            return authProvider.getMetadata().getName();
        }

        @Override
        public int compareTo(@NonNull AuthProviderWithPriority o) {
            return Comparator.comparingInt(AuthProviderWithPriority::getPriority)
                .thenComparing(AuthProviderWithPriority::getName)
                .compare(this, o);
        }
    }

    private Mono<List<SystemSetting.AuthProviderState>> fetchProviderStates() {
        return getSystemConfigMap()
            .map(AuthProviderServiceImpl::getAuthProviderConfig)
            .map(SystemSetting.AuthProvider::getStates)
            .defaultIfEmpty(List.of())
            .subscribeOn(Schedulers.boundedElastic());
    }

    Flux<UserConnection> listMyConnections() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .flatMapMany(username -> {
                var listOptions = ListOptions.builder()
                    .andQuery(QueryFactory.equal("spec.username", username))
                    .andQuery(ExtensionUtil.notDeleting())
                    .build();
                return client.listAll(UserConnection.class, listOptions,
                    ExtensionUtil.defaultSort());
            });
    }

    private ListedAuthProvider convertTo(AuthProvider authProvider) {
        return ListedAuthProvider.builder()
            .name(authProvider.getMetadata().getName())
            .displayName(authProvider.getSpec().getDisplayName())
            .logo(authProvider.getSpec().getLogo())
            .website(authProvider.getSpec().getWebsite())
            .description(authProvider.getSpec().getDescription())
            .authenticationUrl(authProvider.getSpec().getAuthenticationUrl())
            .helpPage(authProvider.getSpec().getHelpPage())
            .bindingUrl(authProvider.getSpec().getBindingUrl())
            .unbindingUrl(authProvider.getSpec().getUnbindUrl())
            .supportsBinding(supportsBinding(authProvider))
            .authType(authProvider.getSpec().getAuthType())
            .isBound(false)
            .enabled(false)
            .privileged(privileged(authProvider))
            .build();
    }

    private static boolean supportsBinding(AuthProvider authProvider) {
        return BooleanUtils.TRUE.equals(MetadataUtil.nullSafeLabels(authProvider)
            .get(AuthProvider.AUTH_BINDING_LABEL));
    }

    private boolean privileged(AuthProvider authProvider) {
        return BooleanUtils.TRUE.equals(MetadataUtil.nullSafeLabels(authProvider)
            .get(AuthProvider.PRIVILEGED_LABEL));
    }

    @NonNull
    private static SystemSetting.AuthProvider getAuthProviderConfig(ConfigMap configMap) {
        if (configMap.getData() == null) {
            configMap.setData(new HashMap<>());
        }
        final Map<String, String> data = configMap.getData();
        String providerGroup = data.get(SystemSetting.AuthProvider.GROUP);

        SystemSetting.AuthProvider authProvider;
        if (StringUtils.isBlank(providerGroup)) {
            authProvider = new SystemSetting.AuthProvider();
        } else {
            authProvider =
                JsonUtils.jsonToObject(providerGroup, SystemSetting.AuthProvider.class);
        }
        if (authProvider.getStates() == null) {
            authProvider.setStates(new ArrayList<>());
        }

        return authProvider;
    }

    private Mono<ConfigMap> updateAuthProviderEnabled(String name, boolean enabled) {
        return Mono.defer(() -> getSystemConfigMap()
                .flatMap(configMap -> {
                    var providerConfig = getAuthProviderConfig(configMap);
                    var stateToFoundOpt = providerConfig.getStates()
                        .stream()
                        .filter(state -> state.getName().equals(name))
                        .findFirst();
                    if (stateToFoundOpt.isEmpty()) {
                        var state = new SystemSetting.AuthProviderState()
                            .setName(name)
                            .setEnabled(enabled);
                        providerConfig.getStates().add(state);
                    } else {
                        stateToFoundOpt.get().setEnabled(enabled);
                    }

                    configMap.getData().put(SystemSetting.AuthProvider.GROUP,
                        JsonUtils.objectToJson(providerConfig));

                    return client.update(configMap);
                })
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<ConfigMap> getSystemConfigMap() {
        var systemFetcher = environmentFetcherProvider.getIfUnique();
        if (systemFetcher == null) {
            return Mono.error(
                new IllegalStateException("No SystemConfigurableEnvironmentFetcher found"));
        }
        return systemFetcher.getConfigMap();
    }
}
