package run.halo.app.core.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EmptyEmailUserIntegrationTest {

    @Autowired
    ReactiveExtensionClient client;

    @Test
    void shouldPersistUserWithoutEmail() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("oauth-empty-email");
        var spec = new User.UserSpec();
        spec.setDisplayName("OAuth Empty Email");
        spec.setRegisteredAt(Instant.parse("2026-08-05T00:00:00Z"));
        spec.setEmailVerified(false);
        user.setSpec(spec);

        client.create(user)
                .then(client.get(User.class, "oauth-empty-email"))
                .as(StepVerifier::create)
                .assertNext(saved -> {
                    assertThat(saved.getSpec().getEmail()).isNull();
                    assertThat(saved.getSpec().getPassword()).isNull();
                })
                .verifyComplete();
    }
}
