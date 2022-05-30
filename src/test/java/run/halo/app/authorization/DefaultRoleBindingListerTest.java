package run.halo.app.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import run.halo.app.identity.authorization.DefaultRoleBindingLister;

/**
 * Tests for {@link DefaultRoleBindingLister}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
public class DefaultRoleBindingListerTest {

    private DefaultRoleBindingLister roleBindingLister;

    @BeforeEach
    void setUp() {
        roleBindingLister = new DefaultRoleBindingLister();
    }

    @Test
    @WithMockUser(username = "test", roles = {"readPost", "readTag"})
    void listWhenAuthorizedWithTwoRoles() {
        Set<String> roleBindings = roleBindingLister.listBoundRoleNames();
        assertThat(roleBindings).isNotNull();
        assertThat(roleBindings.size()).isEqualTo(2);
        assertThat(roleBindings).containsAll(Set.of("readPost", "readTag"));
    }

    @Test
    void listWhenUnauthorizedThenEmpty() {
        Set<String> roleBindings = roleBindingLister.listBoundRoleNames();
        assertThat(roleBindings).isEmpty();
    }
}
