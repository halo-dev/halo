package run.halo.app.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import run.halo.app.core.extension.service.DefaultRoleBindingService;

/**
 * Tests for {@link DefaultRoleBindingService}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultRoleBindingServiceTest {

    private DefaultRoleBindingService roleBindingLister;

    @BeforeEach
    void setUp() {
        roleBindingLister = new DefaultRoleBindingService();
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listWhenAuthorizedRoles() {
        var authorities = List.of(
            new SimpleGrantedAuthority("readFake"),
            new SimpleGrantedAuthority("fake.read"),
            new SimpleGrantedAuthority("ROLE_role.fake.read"),
            new SimpleGrantedAuthority("SCOPE_scope.fake.read"),
            new SimpleGrantedAuthority("SCOPE_ROLE_scope.role.fake.read"));

        Set<String> roleBindings = roleBindingLister.listBoundRoleNames(authorities);
        assertThat(roleBindings).isNotNull();
        assertThat(roleBindings).isEqualTo(Set.of(
            "readFake",
            "fake.read",
            "role.fake.read",
            "scope.fake.read",
            "scope.role.fake.read"));
    }

    @Test
    void listWhenUnauthorizedThenEmpty() {
        var roleBindings = roleBindingLister.listBoundRoleNames(Collections.emptyList());
        assertThat(roleBindings).isEmpty();
    }
}
