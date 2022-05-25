package run.halo.app.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import run.halo.app.identity.authorization.DefaultRoleBindingLister;
import run.halo.app.identity.authorization.RoleBinding;

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
        List<RoleBinding> roleBindings = roleBindingLister.listRoleBindings();
        assertThat(roleBindings).isNotNull();
        assertThat(roleBindings.size()).isEqualTo(2);

        RoleBinding readPostRoleBinding = roleBindings.get(0);
        assertThat(readPostRoleBinding.getMetadata()).isNotNull();
        assertThat(readPostRoleBinding.getMetadata().getName()).isNotNull();
        assertThat(readPostRoleBinding.getSubjects()).allMatch(subject ->
            "test".equals(subject.getName())
                && "User".equals(subject.getKind()));
        assertThat(readPostRoleBinding.getRoleRef().getName()).isEqualTo("readPost");

        RoleBinding readTagRoleBinding = roleBindings.get(1);
        assertThat(readTagRoleBinding.getMetadata()).isNotNull();
        assertThat(readTagRoleBinding.getMetadata().getName()).isNotNull();
        assertThat(readTagRoleBinding.getSubjects()).allMatch(subject ->
            "test".equals(subject.getName())
                && "User".equals(subject.getKind()));
        assertThat(readTagRoleBinding.getRoleRef().getName()).isEqualTo("readTag");
    }

    @Test
    void listWhenUnauthorizedThenEmpty() {
        List<RoleBinding> roleBindings = roleBindingLister.listRoleBindings();
        assertThat(roleBindings).isEmpty();
    }
}
