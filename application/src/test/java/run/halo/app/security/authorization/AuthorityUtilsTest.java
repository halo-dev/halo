package run.halo.app.security.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static run.halo.app.security.authorization.AuthorityUtils.authoritiesToRoles;
import static run.halo.app.security.authorization.AuthorityUtils.containsSuperRole;
import static run.halo.app.security.authorization.AuthorityUtils.isRealUser;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthorityUtilsTest {

    @Test
    void authoritiesToRolesTest() {
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_admin"),
            new SimpleGrantedAuthority("ROLE_owner"),
            new SimpleGrantedAuthority("ROLE_manager"),
            new SimpleGrantedAuthority("faker"),
            new SimpleGrantedAuthority("SCOPE_system:read")
        );

        var roles = authoritiesToRoles(authorities);

        assertEquals(Set.of("admin", "owner", "manager"), roles);
    }

    @Test
    void containsSuperRoleTest() {
        assertTrue(containsSuperRole(Set.of("super-role")));
        assertTrue(containsSuperRole(Set.of("super-role", "admin")));
        assertFalse(containsSuperRole(Set.of("admin")));
    }

    @Test
    void shouldReturnTrueWhenAuthenticationIsRealUser() {
        assertTrue(isRealUser(mock(UsernamePasswordAuthenticationToken.class)));
        assertTrue(isRealUser(mock(RememberMeAuthenticationToken.class)));
    }
}