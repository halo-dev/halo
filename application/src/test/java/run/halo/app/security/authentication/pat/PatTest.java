package run.halo.app.security.authentication.pat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.security.PersonalAccessToken;

@SpringBootTest
@AutoConfigureWebTestClient
class PatTest {

    @Autowired
    WebTestClient webClient;

    @Test
    @WithMockUser(username = "faker", password = "${noop}password", roles = "super-role")
    void generatePat() {
        var requestPat = new PersonalAccessToken();
        var spec = requestPat.getSpec();
        spec.setRoles(List.of("super-role"));
        spec.setName("Fake PAT");
        webClient.post()
            .uri("/apis/api.security.halo.run/v1alpha1/users/-/personalaccesstokens")
            .bodyValue(requestPat)
            .exchange()
            .expectStatus().isOk()
            .expectBody(PersonalAccessToken.class)
            .value(pat -> {
                var annotations = pat.getMetadata().getAnnotations();
                assertTrue(annotations.containsKey("security.halo.run/access-token"));
            });
    }

}
