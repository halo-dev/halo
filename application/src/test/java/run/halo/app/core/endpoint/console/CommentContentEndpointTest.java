package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentContentRequest;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.security.authorization.AttributesRecord;
import run.halo.app.security.authorization.RbacRequestEvaluation;
import run.halo.app.security.authorization.RequestInfoFactory;

@ExtendWith(MockitoExtension.class)
class CommentContentEndpointTest {
    @Mock
    CommentService comments;

    @Mock
    ReplyService replies;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(
                        new CommentEndpoint(comments, replies).endpoint().and(new ReplyEndpoint(replies).endpoint()))
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"comments", "replies"})
    void routesBodyUpdates(String resource) {
        var body = new CommentContentRequest("raw", "<p>raw</p>", 12L);
        if (resource.equals("comments")) {
            when(comments.updateContent("entry", body)).thenReturn(Mono.just(new Comment()));
        } else {
            when(replies.updateContent("entry", body)).thenReturn(Mono.just(new Reply()));
        }
        webClient
                .put()
                .uri("/" + resource + "/entry/content")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk();
        if (resource.equals("comments")) {
            verify(comments).updateContent("entry", body);
        } else {
            verify(replies).updateContent("entry", body);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"comments", "replies"})
    void rejectsMissingBody(String resource) {
        webClient
                .put()
                .uri("/" + resource + "/entry/content")
                .exchange()
                .expectStatus()
                .isBadRequest();
        verifyNoInteractions(comments, replies);
    }

    @ParameterizedTest
    @ValueSource(strings = {"comments", "replies"})
    void onlyManagementRoleAuthorizesBodyUpdates(String resource) {
        var roles = new YamlUnstructuredLoader(new ClassPathResource("extensions/role-template-comment.yaml")).load();
        var request = put("/apis/api.console.halo.run/v1alpha1/" + resource + "/entry/content")
                .build();
        var info = RequestInfoFactory.INSTANCE.newRequestInfo(request);
        assertThat(info.getVerb()).isEqualTo("update");
        assertThat(info.getSubresource()).isEqualTo("content");
        assertThat(roles).hasSize(2);
        for (var unstructured : roles) {
            var role = Unstructured.OBJECT_MAPPER.convertValue(unstructured.getData(), Role.class);
            var allowed = new RbacRequestEvaluation().rulesAllow(new AttributesRecord(info), role.getRules());
            assertThat(allowed).isEqualTo(role.getMetadata().getName().equals("role-template-manage-comments"));
        }
    }
}
