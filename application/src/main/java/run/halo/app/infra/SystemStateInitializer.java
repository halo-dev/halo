package run.halo.app.infra;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static run.halo.app.extension.index.query.Queries.isNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

/**
 * Initializes the {@link SystemState} for legacy installations.
 *
 * <p>Installations created before Halo 2.5.2 have users but no system states ConfigMap. Without this one-time backfill
 * they would be considered as uninitialized and be redirected to the setup page. The backfill only sets the state when
 * it has not been set yet, so it never downgrades or overrides the state managed by the setup flow.
 *
 * @author johnniang
 * @since 2.28.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
class SystemStateInitializer {

    private final ReactiveExtensionClient client;

    @EventListener
    Mono<Void> onApplicationEvent(ExtensionInitializedEvent ignored) {
        return client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
                .map(configMap -> isTrue(SystemState.deserialize(configMap).getIsSetup()))
                .defaultIfEmpty(false)
                .flatMap(initialized -> initialized ? Mono.<Void>empty() : backfillLegacyInstallation())
                .onErrorResume(error -> {
                    log.error("Failed to backfill system states. Startup will continue.", error);
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> backfillLegacyInstallation() {
        return hasUser()
                .filter(Boolean::booleanValue)
                .flatMap(ignored -> SystemState.upsetSystemState(client, systemState -> {
                    // Only initialize the state if it has not been set meanwhile.
                    if (systemState.getIsSetup() == null) {
                        systemState.setIsSetup(true);
                        log.info("Backfilled system states for a legacy installation.");
                    }
                }))
                .then();
    }

    private Mono<Boolean> hasUser() {
        var listOptions = new ListOptions();
        listOptions.setLabelSelector(
                LabelSelector.builder().notEq(User.HIDDEN_USER_LABEL, "true").build());
        listOptions.setFieldSelector(FieldSelector.of(isNull("metadata.deletionTimestamp")));
        return client.listBy(User.class, listOptions, PageRequestImpl.ofSize(1))
                .map(result -> result.getTotal() > 0)
                .defaultIfEmpty(false);
    }
}
