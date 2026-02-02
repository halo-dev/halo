package run.halo.app.security.switchuser;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.web.server.authentication.SwitchUserWebFilter.ROLE_PREVIOUS_ADMINISTRATOR;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

class SwitchUserSecurityContextFactory implements WithSecurityContextFactory<WithSwitchUser> {

    @Override
    public SecurityContext createSecurityContext(WithSwitchUser annotation) {
        var username = annotation.username();
        var roles = annotation.roles();
        var switchToUsername = annotation.targetUsername();
        var switchToRoles = annotation.targetRoles();

        var currentAuthentication =
            authenticated(username, "password", AuthorityUtils.createAuthorityList(roles));
        var switchAuthority =
            new SwitchUserGrantedAuthority(ROLE_PREVIOUS_ADMINISTRATOR, currentAuthentication);

        var targetAuthorities = AuthorityUtils.createAuthorityList(switchToRoles);
        targetAuthorities.add(switchAuthority);
        var authentication = authenticated(switchToUsername, "password",
            targetAuthorities);
        return new SecurityContextImpl(authentication);
    }
}
