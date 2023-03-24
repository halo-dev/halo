package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Integration tests for {@link PostService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase
@WithMockUser(username = "fake-user", password = "fake-password", roles = "fake-super-role")
public class PostIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    RoleService roleService;

    @Autowired
    ReactiveExtensionClient client;

    @BeforeEach
    void setUp() {
        var rule = new Role.PolicyRule.Builder()
            .apiGroups("*")
            .resources("*")
            .verbs("*")
            .build();
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("super-role");
        role.setRules(List.of(rule));
        when(roleService.getMonoRole("authenticated")).thenReturn(Mono.just(role));
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void draftPost() {
        webTestClient.post()
            .uri("/apis/api.console.halo.run/v1alpha1/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(postDraftRequest())
            .exchange()
            .expectBody(Post.class)
            .value(post -> {
                MetadataOperator metadata = post.getMetadata();
                Post.PostSpec spec = post.getSpec();
                assertThat(spec.getTitle()).isEqualTo("无标题文章");
                assertThat(metadata.getCreationTimestamp()).isNotNull();
                assertThat(metadata.getName()).startsWith("post-");
                assertThat(spec.getHeadSnapshot()).isNotNull();
                assertThat(spec.getHeadSnapshot()).isEqualTo(spec.getBaseSnapshot());
                assertThat(spec.getOwner()).isEqualTo("fake-user");

                assertThat(post.getStatus()).isNotNull();
                assertThat(post.getStatus().getPhase()).isEqualTo("DRAFT");
                assertThat(post.getStatus().getConditions().peek().getType()).isEqualTo("DRAFT");
            });
    }

    @Test
    void draftPostAsPublish() {
        PostRequest postRequest = postDraftRequest();
        postRequest.post().getSpec().setPublish(true);
        webTestClient.post()
            .uri("/apis/api.console.halo.run/v1alpha1/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(postRequest)
            .exchange()
            .expectBody(Post.class)
            .value(post -> {
                assertThat(post.getSpec().getReleaseSnapshot()).isNotNull();
                assertThat(post.getSpec().getReleaseSnapshot())
                    .isEqualTo(post.getSpec().getHeadSnapshot());
                assertThat(post.getSpec().getHeadSnapshot())
                    .isEqualTo(post.getSpec().getBaseSnapshot());
            });
    }

    PostRequest postDraftRequest() {
        String s = """
            {
                "post": {
                    "spec": {
                        "title": "无标题文章",
                        "slug": "41c2ad39-21b4-45e4-a36b-5768245a0555",
                        "template": "",
                        "cover": "",
                        "deleted": false,
                        "publish": true,
                        "publishTime": "",
                        "pinned": false,
                        "allowComment": true,
                        "visible": "PUBLIC",
                        "version": 1,
                        "priority": 0,
                        "excerpt": {
                            "autoGenerate": true,
                            "raw": ""
                        },
                        "categories": [],
                        "tags": [],
                        "htmlMetas": []
                    },
                    "apiVersion": "content.halo.run/v1alpha1",
                    "kind": "Post",
                    "metadata": {
                        "name": "",
                        "generateName": "post-"
                    }
                },
                "content": {
                    "raw": "<p>hello world</p>",
                    "content": "<p>hello world</p>",
                    "rawType": "HTML"
                }
            }
            """;
        return JsonUtils.jsonToObject(s, PostRequest.class);
    }
}
