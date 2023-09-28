package run.halo.app.notification;

import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

/**
 * <p>{@link NotifierConfigStore} to store notifier config.</p>
 * <p>It provides methods to fetch and save config for receiver and sender.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
public interface NotifierConfigStore {

    Mono<ObjectNode> fetchReceiverConfig(String notifierDescriptorName);

    Mono<ObjectNode> fetchSenderConfig(String notifierDescriptorName);

    Mono<Void> saveReceiverConfig(String notifierDescriptorName, ObjectNode config);

    Mono<Void> saveSenderConfig(String notifierDescriptorName, ObjectNode config);
}
