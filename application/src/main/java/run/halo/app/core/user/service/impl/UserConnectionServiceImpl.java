package run.halo.app.core.user.service.impl;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.time.Clock;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.UserConnection.UserConnectionSpec;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.event.user.UserConnectionDisconnectedEvent;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.OAuth2UserAlreadyBoundException;
import run.halo.app.infra.utils.JsonUtils;

@Service
public class UserConnectionServiceImpl implements UserConnectionService {

    private final ReactiveExtensionClient client;

    private final ApplicationEventPublisher eventPublisher;

    private Clock clock = Clock.systemDefaultZone();

    public UserConnectionServiceImpl(ReactiveExtensionClient client,
        ApplicationEventPublisher eventPublisher) {
        this.client = client;
        this.eventPublisher = eventPublisher;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<UserConnection> createUserConnection(
        String username,
        String registrationId,
        OAuth2User oauth2User
    ) {
        return getUserConnection(registrationId, username)
            .flatMap(connection -> Mono.<UserConnection>error(
                () -> new OAuth2UserAlreadyBoundException(connection))
            )
            .switchIfEmpty(Mono.defer(() -> {
                var connection = new UserConnection();
                connection.setMetadata(new Metadata());
                var metadata = connection.getMetadata();
                updateUserInfo(metadata, oauth2User);
                metadata.setGenerateName(username + "-");
                connection.setSpec(new UserConnectionSpec());
                var spec = connection.getSpec();
                spec.setUsername(username);
                spec.setProviderUserId(oauth2User.getName());
                spec.setRegistrationId(registrationId);
                spec.setUpdatedAt(clock.instant());
                return client.create(connection);
            }));
    }

    private Mono<UserConnection> updateUserConnection(UserConnection connection,
        OAuth2User oauth2User) {
        connection.getSpec().setUpdatedAt(clock.instant());
        updateUserInfo(connection.getMetadata(), oauth2User);
        return client.update(connection);
    }

    private Mono<UserConnection> getUserConnection(String registrationId, String username) {
        var listOptions = ListOptions.builder()
            .fieldQuery(and(
                equal("spec.registrationId", registrationId),
                equal("spec.username", username)
            ))
            .build();
        return client.listAll(UserConnection.class, listOptions, defaultSort()).next();
    }

    @Override
    public Mono<UserConnection> updateUserConnectionIfPresent(String registrationId,
        OAuth2User oauth2User) {
        var listOptions = ListOptions.builder()
            .fieldQuery(and(
                equal("spec.registrationId", registrationId),
                equal("spec.providerUserId", oauth2User.getName())
            ))
            .build();
        return client.listAll(UserConnection.class, listOptions, defaultSort()).next()
            .flatMap(connection -> updateUserConnection(connection, oauth2User));
    }

    @Override
    public Flux<UserConnection> removeUserConnection(String registrationId, String username) {
        var listOptions = ListOptions.builder()
            .fieldQuery(and(
                equal("spec.registrationId", registrationId),
                equal("spec.username", username)
            ))
            .build();
        return client.listAll(UserConnection.class, listOptions, defaultSort())
            .flatMap(client::delete)
            .doOnNext(deleted ->
                eventPublisher.publishEvent(new UserConnectionDisconnectedEvent(this, deleted))
            );
    }

    private void updateUserInfo(MetadataOperator metadata, OAuth2User oauth2User) {
        var annotations = Optional.ofNullable(metadata.getAnnotations())
            .orElseGet(HashMap::new);
        metadata.setAnnotations(annotations);
        annotations.put(
            "auth.halo.run/oauth2-user-info",
            JsonUtils.objectToJson(oauth2User.getAttributes())
        );
    }
}
