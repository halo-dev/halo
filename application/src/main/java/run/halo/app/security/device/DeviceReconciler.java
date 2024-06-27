package run.halo.app.security.device;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Device;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.router.selector.FieldSelector;

@Component
@RequiredArgsConstructor
public class DeviceReconciler implements Reconciler<Reconciler.Request> {
    private static final int MAX_DEVICES = 10;
    static final String FINALIZER_NAME = "device-protection";
    private final ReactiveSessionRepository<?> sessionRepository;
    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Device.class, request.name())
            .ifPresent(device -> {
                if (isDeleted(device)) {
                    if (removeFinalizers(device.getMetadata(), Set.of(FINALIZER_NAME))) {
                        sessionRepository.deleteById(device.getSpec().getSessionId())
                            .block();
                        client.update(device);
                    }
                    return;
                }
                if (addFinalizers(device.getMetadata(), Set.of(FINALIZER_NAME))) {
                    client.update(device);
                }
                revokeInactiveDevices(device.getSpec().getPrincipalName());
            });
        return Result.doNotRetry();
    }

    private void revokeInactiveDevices(String principalName) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            equal("spec.principalName", principalName))
        );
        client.listAll(Device.class, listOptions,
                Sort.by("metadata.creationTimestamp").descending())
            .stream()
            .skip(MAX_DEVICES)
            .filter(device -> sessionRepository.findById(device.getSpec().getSessionId())
                .blockOptional()
                .isEmpty()
            )
            .forEach(client::delete);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Device())
            .syncAllOnStart(false)
            .build();
    }
}
