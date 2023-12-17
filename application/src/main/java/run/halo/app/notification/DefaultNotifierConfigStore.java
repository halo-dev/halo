package run.halo.app.notification;

import static run.halo.app.extension.MetadataUtil.SYSTEM_FINALIZER;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Secret;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A default implementation of {@link NotifierConfigStore}.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class DefaultNotifierConfigStore implements NotifierConfigStore {
    public static final String SECRET_NAME = "notifier-setting-secret";
    public static final String RECEIVER_KEY = "receiver";
    public static final String SENDER_KEY = "sender";

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ObjectNode> fetchReceiverConfig(String notifierDescriptorName) {
        return fetchConfig(notifierDescriptorName)
            .mapNotNull(setting -> (ObjectNode) setting.get(RECEIVER_KEY))
            .defaultIfEmpty(JsonNodeFactory.instance.objectNode());
    }

    @Override
    public Mono<ObjectNode> fetchSenderConfig(String notifierDescriptorName) {
        return fetchConfig(notifierDescriptorName)
            .mapNotNull(setting -> (ObjectNode) setting.get(SENDER_KEY))
            .defaultIfEmpty(JsonNodeFactory.instance.objectNode());
    }

    @Override
    public Mono<Void> saveReceiverConfig(String notifierDescriptorName, ObjectNode config) {
        return saveConfig(notifierDescriptorName, RECEIVER_KEY, config);
    }

    @Override
    public Mono<Void> saveSenderConfig(String notifierDescriptorName, ObjectNode config) {
        return saveConfig(notifierDescriptorName, SENDER_KEY, config);
    }

    Mono<Void> saveConfig(String notifierDescriptorName, String key, ObjectNode config) {
        return client.fetch(Secret.class, SECRET_NAME)
            .switchIfEmpty(Mono.defer(() -> {
                Secret secret = new Secret();
                secret.setMetadata(new Metadata());
                secret.getMetadata().setName(SECRET_NAME);
                secret.getMetadata().setFinalizers(Set.of(SYSTEM_FINALIZER));
                secret.setStringData(new HashMap<>());
                return client.create(secret);
            }))
            .flatMap(secret -> {
                if (secret.getStringData() == null) {
                    secret.setStringData(new HashMap<>());
                }
                Map<String, String> map = secret.getStringData();
                ObjectNode wrapperNode = JsonNodeFactory.instance.objectNode();
                wrapperNode.set(key, config);
                map.put(resolveKey(notifierDescriptorName), JsonUtils.objectToJson(wrapperNode));
                return client.update(secret);
            })
            .then();
    }

    Mono<ObjectNode> fetchConfig(String notifierDescriptorName) {
        return client.fetch(Secret.class, SECRET_NAME)
            .mapNotNull(Secret::getStringData)
            .mapNotNull(map -> map.get(resolveKey(notifierDescriptorName)))
            .filter(StringUtils::isNotBlank)
            .map(value -> JsonUtils.jsonToObject(value, ObjectNode.class))
            .defaultIfEmpty(JsonNodeFactory.instance.objectNode());
    }

    String resolveKey(String notifierDescriptorName) {
        return notifierDescriptorName + ".json";
    }
}
