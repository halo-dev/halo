package run.halo.app.security;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
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

    @Override
    public Mono<AuthProvider> enable(String name) {
        return client.get(AuthProvider.class, name)
            .flatMap(authProvider -> updateAuthProviderEnabled(enabled -> enabled.add(name))
                .thenReturn(authProvider)
            );
    }

    @Override
    public Mono<AuthProvider> disable(String name) {
        // privileged auth provider cannot be disabled
        return client.get(AuthProvider.class, name)
            .filter(authProvider -> !privileged(authProvider))
            .flatMap(authProvider -> updateAuthProviderEnabled(enabled -> enabled.remove(name))
                .thenReturn(authProvider)
            );
    }

    @Override
    public Mono<List<ListedAuthProvider>> listAll() {
        return client.list(AuthProvider.class, provider ->
                    provider.getMetadata().getDeletionTimestamp() == null,
                Comparator.comparing(item -> item.getMetadata().getCreationTimestamp())
            )
            .map(this::convertTo)
            .collectList()
            .flatMap(providers -> listMyConnections()
                .map(connection -> connection.getSpec().getRegistrationId())
                .collectList()
                .map(connectedNames -> providers.stream()
                    .peek(provider -> {
                        boolean isBound = connectedNames.contains(provider.getName());
                        provider.setIsBound(isBound);
                    })
                    .collect(Collectors.toList())
                )
                .defaultIfEmpty(providers)
            )
            .flatMap(providers -> fetchEnabledAuthProviders()
                .map(names -> providers.stream()
                    .peek(provider -> {
                        boolean enabled = names.contains(provider.getName());
                        provider.setEnabled(enabled);
                    })
                    .collect(Collectors.toList())
                )
                .defaultIfEmpty(providers)
            );
    }

    private Mono<Set<String>> fetchEnabledAuthProviders() {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
            .map(configMap -> {
                SystemSetting.AuthProvider authProvider = getAuthProvider(configMap);
                return authProvider.getEnabled();
            });
    }

    Flux<UserConnection> listMyConnections() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .flatMapMany(username -> client.list(UserConnection.class,
                    persisted -> persisted.getSpec().getUsername().equals(username),
                    Comparator.comparing(item -> item.getMetadata()
                        .getCreationTimestamp())
                )
            );
    }

    private Mono<ConfigMap> updateAuthProviderEnabled(Consumer<Set<String>> consumer) {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
            .switchIfEmpty(Mono.defer(() -> {
                ConfigMap configMap = new ConfigMap();
                configMap.setMetadata(new Metadata());
                configMap.getMetadata().setName(SystemSetting.SYSTEM_CONFIG);
                configMap.setData(new HashMap<>());
                return client.create(configMap);
            }))
            .flatMap(configMap -> {
                SystemSetting.AuthProvider authProvider = getAuthProvider(configMap);
                consumer.accept(authProvider.getEnabled());

                final Map<String, String> data = configMap.getData();
                data.put(SystemSetting.AuthProvider.GROUP,
                    JsonUtils.objectToJson(authProvider));
                return client.update(configMap);
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
    private static SystemSetting.AuthProvider getAuthProvider(ConfigMap configMap) {
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

        if (authProvider.getEnabled() == null) {
            authProvider.setEnabled(new HashSet<>());
        }
        return authProvider;
    }
}
