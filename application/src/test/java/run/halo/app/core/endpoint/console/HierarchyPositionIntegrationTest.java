package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
@WithMockUser(roles = "hierarchy-test")
class HierarchyPositionIntegrationTest {
    private static final String API = "/apis/api.console.halo.run/v1alpha1";

    @TempDir
    static Path workDir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("halo.work-dir", () -> workDir.toString());
    }

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoBean
    RoleService roleService;

    @BeforeEach
    void setUp() {
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("hierarchy-test");
        role.setRules(List.of(new Role.PolicyRule.Builder()
                .apiGroups("*")
                .resources("*")
                .verbs("*")
                .build()));
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        webClient = webClient.mutateWith(csrf());
    }

    @Test
    void movesCategoryMenuBeforeOrphanAndPersistsTheDisplayedHierarchy() {
        createMenuItem("home", null, 0);
        var orphan = createMenuItem("orphan", "missing-parent", 1);
        createMenuItem("directory", null, 2);
        createMenuItem("menu-child", "directory", 0);

        webClient
                .get()
                .uri(API + "/menuitems/-/tree?menuName=primary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[1].menuItem.metadata.name")
                .isEqualTo("orphan");

        webClient
                .put()
                .uri(API + "/menuitems/directory/position")
                .bodyValue(new MenuItemPositionRequest("primary", null, "orphan"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[1].menuItem.metadata.name")
                .isEqualTo("directory");

        webClient
                .get()
                .uri(API + "/menuitems/-/tree?menuName=primary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[*].menuItem.metadata.name")
                .isEqualTo(List.of("home", "directory", "orphan"))
                .jsonPath("$[1].children[0].menuItem.metadata.name")
                .isEqualTo("menu-child");
        var stored = client.get(MenuItem.class, "orphan").block();
        assertThat(stored.getSpec().getParent()).isNull();
        assertThat(stored.getSpec().getPriority()).isEqualTo(2);
        assertThat(stored.getMetadata().getVersion())
                .isGreaterThan(orphan.getMetadata().getVersion());

        webClient
                .put()
                .uri(API + "/menuitems/directory/position")
                .bodyValue(new MenuItemPositionRequest("primary", "menu-child", null))
                .exchange()
                .expectStatus()
                .isBadRequest();
        assertThat(client.get(MenuItem.class, "directory").block().getSpec().getParent())
                .isNull();
    }

    @Test
    void movesCategoryBeforeOrphanAndPersistsTheDisplayedHierarchy() {
        createCategory("root", null, 0);
        var orphan = createCategory("orphan-category", "missing-category", 1);
        createCategory("moved", null, 2);
        createCategory("category-child", "moved", 0);

        webClient
                .get()
                .uri(API + "/categories/-/tree")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[1].category.metadata.name")
                .isEqualTo("orphan-category");

        webClient
                .put()
                .uri(API + "/categories/moved/position")
                .bodyValue(new CategoryPositionRequest(null, "orphan-category"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[1].category.metadata.name")
                .isEqualTo("moved");

        webClient
                .get()
                .uri(API + "/categories/-/tree")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[*].category.metadata.name")
                .isEqualTo(List.of("root", "moved", "orphan-category"))
                .jsonPath("$[1].children[0].category.metadata.name")
                .isEqualTo("category-child");
        var stored = client.get(Category.class, "orphan-category").block();
        assertThat(stored.getSpec().getParent()).isNull();
        assertThat(stored.getSpec().getPriority()).isEqualTo(2);
        assertThat(stored.getMetadata().getVersion())
                .isGreaterThan(orphan.getMetadata().getVersion());

        webClient
                .put()
                .uri(API + "/categories/moved/position")
                .bodyValue(new CategoryPositionRequest("category-child", null))
                .exchange()
                .expectStatus()
                .isBadRequest();
        assertThat(client.get(Category.class, "moved").block().getSpec().getParent())
                .isNull();
    }

    private MenuItem createMenuItem(String name, String parent, int priority) {
        var item = new MenuItem();
        item.setMetadata(new Metadata());
        item.getMetadata().setName(name);
        item.setSpec(new MenuItem.MenuItemSpec());
        item.getSpec().setDisplayName(name);
        item.getSpec().setMenuName("primary");
        item.getSpec().setParent(parent);
        item.getSpec().setPriority(priority);
        if ("directory".equals(name)) {
            item.getSpec().setTargetRef(Ref.of("referenced-category", Category.GVK));
        }
        return client.create(item).block();
    }

    private Category createCategory(String name, String parent, int priority) {
        var category = new Category();
        category.setMetadata(new Metadata());
        category.getMetadata().setName(name);
        category.setSpec(new Category.CategorySpec());
        category.getSpec().setDisplayName(name);
        category.getSpec().setSlug(name);
        category.getSpec().setParent(parent);
        category.getSpec().setPriority(priority);
        return client.create(category).block();
    }
}
